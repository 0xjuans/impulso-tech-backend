-- =============================================================================
-- V31: Datos de prueba para comunidad, evaluaciones, laboratorios y proyectos.
--
-- Extiende el sembrado inicial (V30) para permitir probar los flujos que
-- todavía quedaban vacíos en el entorno. Mantiene el mismo enfoque:
--
--   * Idempotente: cada INSERT usa WHERE NOT EXISTS con una clave natural
--     estable (por ejemplo el título de la publicación o el nombre del
--     laboratorio) para no duplicar filas al re-ejecutarse.
--   * No borra ni modifica datos reales del usuario.
--   * Resuelve dinámicamente los usuarios autor y estudiante para funcionar
--     tanto en un entorno recién bootstrapado como en uno que ya tenga
--     cuentas creadas.
-- =============================================================================

DO $$
DECLARE
    v_author_id     BIGINT;
    v_student_id    BIGINT;
    v_post_id       BIGINT;
    v_reply_id      BIGINT;
    v_lesson_id     BIGINT;
    v_evaluation_id BIGINT;
    v_course_web    BIGINT;
    v_course_spring BIGINT;
    v_route_web     BIGINT;
BEGIN
    -- ----- Selección de usuarios -------------------------------------------
    SELECT id INTO v_author_id
    FROM users WHERE role = 'INSTRUCTOR' AND status = 'ACTIVA'
    ORDER BY created_at LIMIT 1;

    IF v_author_id IS NULL THEN
        SELECT id INTO v_author_id
        FROM users WHERE role = 'ADMINISTRADOR' AND status = 'ACTIVA'
        ORDER BY created_at LIMIT 1;
    END IF;

    SELECT id INTO v_student_id
    FROM users WHERE role = 'ESTUDIANTE' AND status = 'ACTIVA'
    ORDER BY created_at LIMIT 1;

    -- Si no hay ningún estudiante, usamos al mismo autor para que las
    -- filas de comunidad no queden sin dueño y las FKs se cumplan.
    IF v_student_id IS NULL THEN
        v_student_id := v_author_id;
    END IF;

    IF v_author_id IS NULL THEN
        RAISE NOTICE 'V31 seed: no hay usuarios activos, se omite la carga.';
        RETURN;
    END IF;

    SELECT id INTO v_route_web     FROM learning_routes WHERE name = 'Fundamentos de programación web' LIMIT 1;
    SELECT id INTO v_course_web    FROM courses         WHERE name = 'HTML y CSS desde cero'           LIMIT 1;
    SELECT id INTO v_course_spring FROM courses         WHERE name = 'API REST con Spring Boot'        LIMIT 1;

    -- ----- Comunidad: publicación 1 y sus respuestas -----------------------
    INSERT INTO community_posts (author_id, title, description, code_snippet, tags,
                                 related_type, related_id)
    SELECT v_student_id,
           '¿Cómo centro un div con Flexbox?',
           '<p>Llevo un rato peleando con centrar vertical y horizontalmente un <code>div</code> dentro de otro. He probado con <code>margin: auto</code> pero no me funciona en el eje vertical. ¿Alguna sugerencia?</p>',
           E'.contenedor {\n  display: flex;\n  /* ¿qué falta? */\n}',
           'css, flexbox, layout',
           'COURSE', v_course_web
    WHERE NOT EXISTS (
        SELECT 1 FROM community_posts WHERE title = '¿Cómo centro un div con Flexbox?'
    );

    SELECT id INTO v_post_id FROM community_posts WHERE title = '¿Cómo centro un div con Flexbox?' LIMIT 1;

    INSERT INTO community_replies (post_id, author_id, content, code_snippet, helpful_count)
    SELECT v_post_id, v_author_id,
           '<p>Necesitas dos propiedades más en el contenedor:</p><ul><li><code>justify-content: center</code> (eje principal)</li><li><code>align-items: center</code> (eje transversal)</li></ul><p>Con eso queda perfectamente centrado.</p>',
           E'.contenedor {\n  display: flex;\n  justify-content: center;\n  align-items: center;\n  min-height: 100vh;\n}',
           3
    WHERE NOT EXISTS (
        SELECT 1 FROM community_replies
        WHERE post_id = v_post_id AND author_id = v_author_id
    );

    SELECT id INTO v_reply_id FROM community_replies
    WHERE post_id = v_post_id AND author_id = v_author_id LIMIT 1;

    -- Marcamos la respuesta del instructor como aceptada.
    UPDATE community_posts
    SET accepted_reply_id = v_reply_id
    WHERE id = v_post_id AND accepted_reply_id IS NULL;

    INSERT INTO community_replies (post_id, author_id, content, helpful_count)
    SELECT v_post_id, v_student_id,
           '<p>¡Funcionó! Gracias, era exactamente el problema. Me faltaba entender la diferencia entre los dos ejes.</p>',
           1
    WHERE NOT EXISTS (
        SELECT 1 FROM community_replies
        WHERE post_id = v_post_id AND content LIKE '%¡Funcionó!%'
    );

    -- ----- Comunidad: publicación 2 sin respuestas -------------------------
    INSERT INTO community_posts (author_id, title, description, tags, related_type, related_id)
    SELECT v_student_id,
           'Diferencia entre let, const y var',
           '<p>Sé que <code>var</code> es "el viejo", pero ¿cuál es la diferencia práctica entre <code>let</code> y <code>const</code>?</p><blockquote>Solo he visto tutoriales rápidos, quiero entender el <em>por qué</em>.</blockquote>',
           'javascript, fundamentos',
           'LEARNING_ROUTE', v_route_web
    WHERE NOT EXISTS (
        SELECT 1 FROM community_posts WHERE title = 'Diferencia entre let, const y var'
    );

    -- ----- Comunidad: publicación 3 sobre Spring ---------------------------
    INSERT INTO community_posts (author_id, title, description, tags, related_type, related_id)
    SELECT v_student_id,
           'Spring devuelve 403 en mis endpoints públicos',
           '<p>Configuré Spring Security y todo lo que expongo devuelve <strong>403</strong> aunque haya declarado <code>permitAll()</code>. ¿Qué se me está escapando?</p>',
           'spring, seguridad, java',
           'COURSE', v_course_spring
    WHERE NOT EXISTS (
        SELECT 1 FROM community_posts WHERE title = 'Spring devuelve 403 en mis endpoints públicos'
    );

    -- ----- Evaluación asociada a una lección de HTML/CSS -------------------
    SELECT l.id INTO v_lesson_id
    FROM lessons l
    JOIN course_modules m ON l.module_id = m.id
    JOIN courses c        ON m.course_id  = c.id
    WHERE c.name = 'HTML y CSS desde cero'
      AND m.order_index = 1
      AND l.order_index = 1
    LIMIT 1;

    IF v_lesson_id IS NOT NULL THEN
        INSERT INTO evaluations (lesson_id, name, description, instructions,
                                 time_limit_minutes, passing_percentage,
                                 max_attempts, order_index, status)
        SELECT v_lesson_id,
               'Quiz: fundamentos de HTML',
               '<p>Verifica lo aprendido en la lección de estructura mínima y etiquetas semánticas.</p>',
               '<p>Responde las 3 preguntas. Cuentas con 15 minutos y hasta 2 intentos.</p>',
               15, 70, 2, 1, 'PUBLICADO'
        WHERE NOT EXISTS (
            SELECT 1 FROM evaluations WHERE lesson_id = v_lesson_id AND order_index = 1
        );

        SELECT id INTO v_evaluation_id FROM evaluations
        WHERE lesson_id = v_lesson_id AND order_index = 1 LIMIT 1;

        INSERT INTO evaluation_questions (evaluation_id, order_index, type, question_text, score, config)
        SELECT v_evaluation_id, 1, 'SELECCION_MULTIPLE',
               '¿Cuál etiqueta define el contenido principal de una página?',
               1,
               '{"options":["<header>","<main>","<section>","<article>"],"correctIndex":1}'
        WHERE NOT EXISTS (
            SELECT 1 FROM evaluation_questions WHERE evaluation_id = v_evaluation_id AND order_index = 1
        );

        INSERT INTO evaluation_questions (evaluation_id, order_index, type, question_text, score, config)
        SELECT v_evaluation_id, 2, 'VERDADERO_FALSO',
               'La etiqueta <div> tiene significado semántico.',
               1,
               '{"correct":false}'
        WHERE NOT EXISTS (
            SELECT 1 FROM evaluation_questions WHERE evaluation_id = v_evaluation_id AND order_index = 2
        );

        INSERT INTO evaluation_questions (evaluation_id, order_index, type, question_text, score, config)
        SELECT v_evaluation_id, 3, 'RESPUESTA_CORTA',
               'Escribe el DOCTYPE de HTML5.',
               2,
               '{"expected":"<!DOCTYPE html>","caseSensitive":false}'
        WHERE NOT EXISTS (
            SELECT 1 FROM evaluation_questions WHERE evaluation_id = v_evaluation_id AND order_index = 3
        );
    END IF;

    -- ----- Laboratorios -----------------------------------------------------
    INSERT INTO labs (title, description, instructions, language, starter_code,
                      expected_output, execution_timeout_ms, course_id, lesson_id,
                      instructor_id, status)
    SELECT 'Hola mundo con Python',
           '<p>Tu primer script en Python: imprime un saludo a la consola.</p>',
           '<p>Modifica <code>hola.py</code> para que imprima <em>"Hola, Impulso!"</em>.</p>',
           'python',
           E'# hola.py\nprint("Hola")\n',
           'Hola, Impulso!',
           3000, v_course_web, v_lesson_id, v_author_id, 'PUBLICADO'
    WHERE NOT EXISTS (SELECT 1 FROM labs WHERE title = 'Hola mundo con Python');

    INSERT INTO labs (title, description, instructions, language, starter_code,
                      expected_output, execution_timeout_ms, course_id,
                      instructor_id, status)
    SELECT 'Endpoint GET con Spring Boot',
           '<p>Crea un endpoint <code>/api/hello</code> que devuelva un saludo.</p>',
           '<p>Completa el controlador de <code>HelloController.java</code> devolviendo la cadena "Hola desde Spring".</p>',
           'java',
           E'@RestController\npublic class HelloController {\n    // TODO: agregar el endpoint\n}\n',
           'Hola desde Spring',
           5000, v_course_spring, v_author_id, 'PUBLICADO'
    WHERE NOT EXISTS (SELECT 1 FROM labs WHERE title = 'Endpoint GET con Spring Boot');

    -- ----- Proyectos -------------------------------------------------------
    INSERT INTO projects (name, description, objective, instructions, requirements,
                          difficulty, technologies, evaluation_criteria, max_score,
                          xp_reward, status, instructor_id, learning_route_id, course_id)
    SELECT 'Portafolio personal responsivo',
           '<p>Construye tu portafolio como página estática publicable en GitHub Pages o Vercel.</p>',
           'Aplicar los conceptos de HTML semántico, CSS Grid y accesibilidad.',
           '<p>La página debe tener secciones de <strong>Sobre mí</strong>, <strong>Proyectos</strong> y <strong>Contacto</strong>. Debe verse bien en móvil y escritorio.</p>',
           E'- Diseño propio (no template)\n- Formulario de contacto (mailto o servicio)\n- Publicada en GitHub Pages, Netlify o Vercel',
           'PRINCIPIANTE', 'HTML, CSS',
           E'- Semántica correcta (20 %)\n- Responsividad (30 %)\n- Estética y coherencia visual (30 %)\n- Accesibilidad AA (20 %)',
           100, 200, 'PUBLICADO', v_author_id, v_route_web, v_course_web
    WHERE NOT EXISTS (SELECT 1 FROM projects WHERE name = 'Portafolio personal responsivo');

    INSERT INTO projects (name, description, objective, instructions, requirements,
                          difficulty, technologies, evaluation_criteria, max_score,
                          xp_reward, status, instructor_id, course_id)
    SELECT 'API de lista de tareas',
           '<p>Diseña e implementa una API REST para administrar tareas de un usuario autenticado.</p>',
           'Practicar diseño REST, JPA y seguridad con Spring.',
           '<p>Modela las entidades <code>User</code> y <code>Task</code>, expón CRUD sobre tareas y protégelo con JWT.</p>',
           E'- Endpoints REST /api/tasks\n- Autenticación JWT\n- Persistencia con Postgres y Flyway\n- Pruebas de servicio y de controlador',
           'INTERMEDIO', 'Java, Spring Boot, PostgreSQL',
           E'- Diseño REST (25 %)\n- Seguridad (25 %)\n- Cobertura de pruebas (25 %)\n- Documentación en README (25 %)',
           100, 300, 'PUBLICADO', v_author_id, v_course_spring
    WHERE NOT EXISTS (SELECT 1 FROM projects WHERE name = 'API de lista de tareas');

END $$;
