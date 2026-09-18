-- =============================================================================
-- V30: Datos de prueba de contenido educativo.
--
-- Poblado inicial para poder ejercitar los flujos del frontend sin tener que
-- crear a mano rutas, cursos, módulos, lecciones, retos y recursos.
--
-- Idempotencia: se usa WHERE NOT EXISTS con nombres únicos (o pares
-- (course_id, order_index) en el caso de módulos y lecciones) para evitar
-- duplicar filas si Flyway re-ejecutara este script en un entorno donde ya
-- se corrió parcialmente. Ninguna operación DELETE se realiza; los datos
-- reales del usuario permanecen intactos.
--
-- Autor referenciado: se toma el primer usuario con rol INSTRUCTOR y, si no
-- existe, se cae al primer ADMINISTRADOR. Así el script funciona tanto en un
-- entorno recién bootstrapado (solo hay el admin del arranque) como en uno
-- que ya tenga instructores creados.
-- =============================================================================

DO $$
DECLARE
    v_author_id BIGINT;
BEGIN
    SELECT id INTO v_author_id
    FROM users
    WHERE role = 'INSTRUCTOR' AND status = 'ACTIVA'
    ORDER BY created_at
    LIMIT 1;

    IF v_author_id IS NULL THEN
        SELECT id INTO v_author_id
        FROM users
        WHERE role = 'ADMINISTRADOR' AND status = 'ACTIVA'
        ORDER BY created_at
        LIMIT 1;
    END IF;

    IF v_author_id IS NULL THEN
        RAISE NOTICE 'V30 seed: no hay usuarios activos, se omite la carga de datos de prueba.';
        RETURN;
    END IF;

    -- ----- Rutas de aprendizaje ---------------------------------------------
    INSERT INTO learning_routes (name, description, objective, difficulty,
                                 estimated_duration_hours, technologies,
                                 status, instructor_id)
    SELECT 'Fundamentos de programación web',
           '<p>Aprende los pilares del desarrollo web moderno: <strong>HTML</strong>, <strong>CSS</strong> y <strong>JavaScript</strong>. Al terminar podrás construir tus primeras páginas interactivas.</p>',
           'Dar al estudiante una base sólida para continuar con frameworks front-end.',
           'PRINCIPIANTE', 40, 'HTML, CSS, JavaScript',
           'PUBLICADO', v_author_id
    WHERE NOT EXISTS (SELECT 1 FROM learning_routes WHERE name = 'Fundamentos de programación web');

    INSERT INTO learning_routes (name, description, objective, difficulty,
                                 estimated_duration_hours, technologies,
                                 status, instructor_id)
    SELECT 'Backend con Java y Spring Boot',
           '<p>Ruta orientada a construir APIs REST robustas con Spring Boot 3, seguridad con JWT y persistencia con PostgreSQL.</p>',
           'Preparar al estudiante para trabajar en un equipo de backend con Java.',
           'INTERMEDIO', 80, 'Java, Spring Boot, PostgreSQL, Docker',
           'PUBLICADO', v_author_id
    WHERE NOT EXISTS (SELECT 1 FROM learning_routes WHERE name = 'Backend con Java y Spring Boot');

    INSERT INTO learning_routes (name, description, objective, difficulty,
                                 estimated_duration_hours, technologies,
                                 status, instructor_id)
    SELECT 'Angular avanzado y buenas prácticas',
           '<p>Profundiza en Angular 18: signals, control flow, testing con Karma, arquitecturas escalables y optimización de bundle.</p>',
           'Elevar el nivel del estudiante que ya conoce Angular básico.',
           'AVANZADO', 60, 'Angular, TypeScript, RxJS',
           'BORRADOR', v_author_id
    WHERE NOT EXISTS (SELECT 1 FROM learning_routes WHERE name = 'Angular avanzado y buenas prácticas');

    -- ----- Cursos -----------------------------------------------------------
    INSERT INTO courses (name, description, objective, difficulty,
                         estimated_duration_hours, technology,
                         learning_route_id, instructor_id, status,
                         generates_certificate)
    SELECT 'HTML y CSS desde cero',
           '<p>Curso introductorio a las tecnologías base de la web. Termina construyendo tu propio portafolio personal.</p>',
           'Que el estudiante escriba páginas estáticas semánticas y responsivas.',
           'PRINCIPIANTE', 16, 'HTML, CSS',
           (SELECT id FROM learning_routes WHERE name = 'Fundamentos de programación web'),
           v_author_id, 'PUBLICADO', TRUE
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE name = 'HTML y CSS desde cero');

    INSERT INTO courses (name, description, objective, difficulty,
                         estimated_duration_hours, technology,
                         learning_route_id, instructor_id, status,
                         generates_certificate)
    SELECT 'JavaScript esencial',
           '<p>Sintaxis del lenguaje, tipos, funciones, promesas y manipulación del DOM.</p>',
           'Preparar al estudiante para trabajar con cualquier framework front-end.',
           'PRINCIPIANTE', 20, 'JavaScript',
           (SELECT id FROM learning_routes WHERE name = 'Fundamentos de programación web'),
           v_author_id, 'PUBLICADO', TRUE
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE name = 'JavaScript esencial');

    INSERT INTO courses (name, description, objective, difficulty,
                         estimated_duration_hours, technology,
                         learning_route_id, instructor_id, status,
                         generates_certificate)
    SELECT 'API REST con Spring Boot',
           '<p>Construye desde cero una API REST con Spring Boot 3, Spring Security, JPA y Flyway.</p>',
           'Terminar con un backend desplegable en producción.',
           'INTERMEDIO', 32, 'Java, Spring Boot',
           (SELECT id FROM learning_routes WHERE name = 'Backend con Java y Spring Boot'),
           v_author_id, 'PUBLICADO', TRUE
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE name = 'API REST con Spring Boot');

    INSERT INTO courses (name, description, objective, difficulty,
                         estimated_duration_hours, technology,
                         learning_route_id, instructor_id, status,
                         generates_certificate)
    SELECT 'Postgres para desarrolladores backend',
           '<p>Modela datos relacionales, escribe consultas eficientes, migraciones con Flyway y depuración con EXPLAIN.</p>',
           'Que el estudiante domine el motor que sostiene la mayoría de sistemas.',
           'INTERMEDIO', 18, 'PostgreSQL, SQL',
           (SELECT id FROM learning_routes WHERE name = 'Backend con Java y Spring Boot'),
           v_author_id, 'BORRADOR', FALSE
    WHERE NOT EXISTS (SELECT 1 FROM courses WHERE name = 'Postgres para desarrolladores backend');

    -- ----- Módulos y lecciones del curso HTML y CSS -------------------------
    INSERT INTO course_modules (course_id, name, description, objective, order_index, status)
    SELECT (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero'),
           'Introducción a HTML',
           '<p>Etiquetas semánticas, estructura de un documento y atributos básicos.</p>',
           'Escribir HTML válido y accesible.',
           1, 'PUBLICADO'
    WHERE NOT EXISTS (
        SELECT 1 FROM course_modules
        WHERE course_id = (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero')
          AND order_index = 1);

    INSERT INTO course_modules (course_id, name, description, objective, order_index, status)
    SELECT (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero'),
           'Estilos con CSS',
           '<p>Selectores, box model, layout con Flexbox y Grid.</p>',
           'Componer diseños responsivos sin frameworks.',
           2, 'PUBLICADO'
    WHERE NOT EXISTS (
        SELECT 1 FROM course_modules
        WHERE course_id = (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero')
          AND order_index = 2);

    INSERT INTO lessons (module_id, title, description, content, estimated_duration_minutes, order_index, status)
    SELECT (SELECT id FROM course_modules
            WHERE course_id = (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero')
              AND order_index = 1),
           'Estructura mínima de una página',
           'Doctype, head y body.',
           '<h3>Estructura mínima</h3><p>Todo documento HTML empieza con <code>&lt;!doctype html&gt;</code> seguido de <code>&lt;html&gt;</code>, <code>&lt;head&gt;</code> y <code>&lt;body&gt;</code>.</p>',
           25, 1, 'PUBLICADO'
    WHERE NOT EXISTS (
        SELECT 1 FROM lessons
        WHERE module_id = (SELECT id FROM course_modules
                           WHERE course_id = (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero')
                             AND order_index = 1)
          AND order_index = 1);

    INSERT INTO lessons (module_id, title, description, content, estimated_duration_minutes, order_index, status)
    SELECT (SELECT id FROM course_modules
            WHERE course_id = (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero')
              AND order_index = 1),
           'Etiquetas semánticas',
           'header, main, article, section, footer.',
           '<h3>Semántica</h3><p>Prefiere <code>&lt;article&gt;</code>, <code>&lt;section&gt;</code> y <code>&lt;nav&gt;</code> a divs genéricos: mejora la accesibilidad y el SEO.</p>',
           30, 2, 'PUBLICADO'
    WHERE NOT EXISTS (
        SELECT 1 FROM lessons
        WHERE module_id = (SELECT id FROM course_modules
                           WHERE course_id = (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero')
                             AND order_index = 1)
          AND order_index = 2);

    INSERT INTO lessons (module_id, title, description, content, estimated_duration_minutes, order_index, status)
    SELECT (SELECT id FROM course_modules
            WHERE course_id = (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero')
              AND order_index = 2),
           'Flexbox en 15 minutos',
           'Alineación de elementos con flex-direction, justify-content y align-items.',
           '<h3>Flexbox</h3><p>Aplica <code>display: flex</code> al contenedor y controla la dirección con <code>flex-direction</code>. Alinea con <code>justify-content</code> (eje principal) y <code>align-items</code> (eje transversal).</p>',
           15, 1, 'PUBLICADO'
    WHERE NOT EXISTS (
        SELECT 1 FROM lessons
        WHERE module_id = (SELECT id FROM course_modules
                           WHERE course_id = (SELECT id FROM courses WHERE name = 'HTML y CSS desde cero')
                             AND order_index = 2)
          AND order_index = 1);

    -- ----- Retos ------------------------------------------------------------
    INSERT INTO challenges (name, description, objective, instructions, difficulty,
                            allowed_languages, io_examples, restrictions,
                            public_test_cases, hidden_test_cases,
                            xp_reward, estimated_minutes, status, instructor_id)
    SELECT 'Palíndromo alfanumérico',
           '<p>Determina si una cadena es palíndromo considerando únicamente caracteres alfanuméricos e ignorando mayúsculas y minúsculas.</p>',
           'Practicar manipulación de cadenas con dos punteros.',
           '<p>Implementa una función <code>esPalindromo(texto)</code> que retorne un booleano.</p><ul><li>Ignora signos y espacios.</li><li>Ignora mayúsculas y minúsculas.</li><li>Cadena vacía se considera palíndromo.</li></ul>',
           'PRINCIPIANTE', 'python, javascript, java',
           E'entrada: "Anita lava la tina"\nsalida:  true\n\nentrada: "hola"\nsalida:  false',
           E'Tamaño máximo: 10000 caracteres.\nComplejidad esperada: O(n).',
           E'[\n  { "input": "Anita lava la tina", "expected": true },\n  { "input": "hola mundo",         "expected": false }\n]',
           E'[\n  { "input": "A man, a plan, a canal: Panama", "expected": true },\n  { "input": "!!!@@@###",                      "expected": true }\n]',
           50, 20, 'PUBLICADO', v_author_id
    WHERE NOT EXISTS (SELECT 1 FROM challenges WHERE name = 'Palíndromo alfanumérico');

    INSERT INTO challenges (name, description, objective, instructions, difficulty,
                            allowed_languages, io_examples, restrictions,
                            xp_reward, estimated_minutes, status, instructor_id)
    SELECT 'FizzBuzz clásico',
           '<p>Imprime los números del 1 al 100 sustituyendo múltiplos de 3 por <em>Fizz</em>, múltiplos de 5 por <em>Buzz</em> y de ambos por <em>FizzBuzz</em>.</p>',
           'Practicar bucles y condicionales.',
           '<p>Escribe la función <code>fizzBuzz()</code> que retorne una lista de strings con las 100 salidas.</p>',
           'PRINCIPIANTE', 'python, javascript',
           E'salida[0]  = "1"\nsalida[2]  = "Fizz"\nsalida[14] = "FizzBuzz"',
           'Sin usar librerías externas.',
           30, 10, 'PUBLICADO', v_author_id
    WHERE NOT EXISTS (SELECT 1 FROM challenges WHERE name = 'FizzBuzz clásico');

    INSERT INTO challenges (name, description, objective, instructions, difficulty,
                            allowed_languages, xp_reward, estimated_minutes,
                            status, instructor_id, learning_route_id, course_id)
    SELECT 'Dos sumandos',
           '<p>Dado un arreglo de enteros y un objetivo, retorna los índices de los dos números que suman el objetivo.</p>',
           'Aplicar hashmaps para lograr complejidad O(n).',
           '<p>Firma: <code>dosSumandos(nums, objetivo) -> [i, j]</code>. Se garantiza una única solución y no se puede usar el mismo elemento dos veces.</p>',
           'INTERMEDIO', 'python, javascript, java', 80, 25,
           'PUBLICADO', v_author_id,
           (SELECT id FROM learning_routes WHERE name = 'Fundamentos de programación web'),
           (SELECT id FROM courses         WHERE name = 'JavaScript esencial')
    WHERE NOT EXISTS (SELECT 1 FROM challenges WHERE name = 'Dos sumandos');

    INSERT INTO challenges (name, description, objective, difficulty,
                            allowed_languages, xp_reward, status, instructor_id)
    SELECT 'Invertir árbol binario',
           '<p>Dado un árbol binario, invierte sus subárboles izquierdo y derecho de forma recursiva.</p>',
           'Practicar recursión sobre estructuras de datos.',
           'AVANZADO', 'python, java',
           120, 'BORRADOR', v_author_id
    WHERE NOT EXISTS (SELECT 1 FROM challenges WHERE name = 'Invertir árbol binario');

    -- ----- Recursos educativos ----------------------------------------------
    INSERT INTO educational_resources (name, description, type, topic, technology,
                                        difficulty, author, resource_url, status,
                                        published_at, created_by, learning_route_id)
    SELECT 'MDN — Introducción a HTML',
           'Guía oficial de Mozilla sobre HTML, mantenida por la comunidad.',
           'ENLACE_EXTERNO', 'HTML', 'HTML',
           'PRINCIPIANTE', 'Mozilla',
           'https://developer.mozilla.org/es/docs/Learn/HTML/Introduction_to_HTML',
           'PUBLICADO', NOW(), v_author_id,
           (SELECT id FROM learning_routes WHERE name = 'Fundamentos de programación web')
    WHERE NOT EXISTS (SELECT 1 FROM educational_resources WHERE name = 'MDN — Introducción a HTML');

    INSERT INTO educational_resources (name, description, type, topic, technology,
                                        difficulty, author, resource_url, status,
                                        published_at, created_by, learning_route_id)
    SELECT 'Guía Flexbox de CSS-Tricks',
           'Referencia visual completa de todas las propiedades de Flexbox.',
           'GUIA', 'Flexbox', 'CSS',
           'PRINCIPIANTE', 'CSS-Tricks',
           'https://css-tricks.com/snippets/css/a-guide-to-flexbox/',
           'PUBLICADO', NOW(), v_author_id,
           (SELECT id FROM learning_routes WHERE name = 'Fundamentos de programación web')
    WHERE NOT EXISTS (SELECT 1 FROM educational_resources WHERE name = 'Guía Flexbox de CSS-Tricks');

    INSERT INTO educational_resources (name, description, type, topic, technology,
                                        difficulty, author, resource_url, status,
                                        published_at, created_by, learning_route_id)
    SELECT 'Spring Boot Reference',
           'Documentación oficial de Spring Boot 3 en un solo sitio.',
           'DOCUMENTO', 'Spring Boot', 'Java',
           'INTERMEDIO', 'VMware',
           'https://docs.spring.io/spring-boot/docs/current/reference/html/',
           'PUBLICADO', NOW(), v_author_id,
           (SELECT id FROM learning_routes WHERE name = 'Backend con Java y Spring Boot')
    WHERE NOT EXISTS (SELECT 1 FROM educational_resources WHERE name = 'Spring Boot Reference');

    INSERT INTO educational_resources (name, description, type, topic, technology,
                                        difficulty, author, resource_url, status,
                                        published_at, created_by)
    SELECT 'Video: JavaScript en 100 segundos',
           'Introducción rápida al lenguaje y sus fortalezas.',
           'VIDEO', 'JavaScript', 'JavaScript',
           'PRINCIPIANTE', 'Fireship',
           'https://www.youtube.com/watch?v=DHjqpvDnNGE',
           'PUBLICADO', NOW(), v_author_id
    WHERE NOT EXISTS (SELECT 1 FROM educational_resources WHERE name = 'Video: JavaScript en 100 segundos');

END $$;
