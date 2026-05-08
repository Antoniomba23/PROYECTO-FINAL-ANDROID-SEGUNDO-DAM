from docx import Document
from docx.shared import Pt, RGBColor, Inches, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.style import WD_STYLE_TYPE
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
import os

doc = Document()

# ── Márgenes ──────────────────────────────────────────────────────────────────
for section in doc.sections:
    section.top_margin    = Cm(2.5)
    section.bottom_margin = Cm(2.5)
    section.left_margin   = Cm(3)
    section.right_margin  = Cm(2.5)

# ── Helpers ───────────────────────────────────────────────────────────────────
def h1(texto):
    p = doc.add_heading(texto, level=1)
    p.runs[0].font.color.rgb = RGBColor(0x1A, 0x73, 0xE8)
    return p

def h2(texto):
    p = doc.add_heading(texto, level=2)
    p.runs[0].font.color.rgb = RGBColor(0x18, 0x4F, 0xA5)
    return p

def h3(texto):
    return doc.add_heading(texto, level=3)

def p(texto, bold=False, italic=False, size=11):
    para = doc.add_paragraph()
    run = para.add_run(texto)
    run.bold = bold
    run.italic = italic
    run.font.size = Pt(size)
    return para

def tabla(headers, rows):
    t = doc.add_table(rows=1+len(rows), cols=len(headers))
    t.style = 'Table Grid'
    hdr = t.rows[0].cells
    for i, h in enumerate(headers):
        hdr[i].text = h
        for run in hdr[i].paragraphs[0].runs:
            run.bold = True
        hdr[i].paragraphs[0].runs[0].font.color.rgb = RGBColor(0xFF,0xFF,0xFF)
        tc = hdr[i]._tc
        tcPr = tc.get_or_add_tcPr()
        shd = OxmlElement('w:shd')
        shd.set(qn('w:fill'), '1A73E8')
        shd.set(qn('w:color'), 'auto')
        shd.set(qn('w:val'), 'clear')
        tcPr.append(shd)
    for ri, row in enumerate(rows):
        cells = t.rows[ri+1].cells
        for ci, val in enumerate(row):
            cells[ci].text = val
    doc.add_paragraph()

def imagen_placeholder(nombre, descripcion):
    """Añade un bloque gris de placeholder para la captura."""
    p_img = doc.add_paragraph()
    p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p_img.add_run(f"[ CAPTURA: {nombre} ]")
    run.font.size = Pt(10)
    run.font.color.rgb = RGBColor(0x88, 0x88, 0x88)
    run.bold = True
    cap = doc.add_paragraph(descripcion)
    cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    cap.runs[0].font.italic = True
    cap.runs[0].font.size = Pt(9)
    cap.runs[0].font.color.rgb = RGBColor(0x55, 0x55, 0x55)
    doc.add_paragraph()

# ══════════════════════════════════════════════════════════════════════════════
# PORTADA
# ══════════════════════════════════════════════════════════════════════════════
doc.add_paragraph()
doc.add_paragraph()
titulo = doc.add_paragraph()
titulo.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = titulo.add_run("MEMORIA TÉCNICA DEL PROYECTO FINAL")
r.bold = True; r.font.size = Pt(20)
r.font.color.rgb = RGBColor(0x1A, 0x73, 0xE8)

subtitulo = doc.add_paragraph()
subtitulo.alignment = WD_ALIGN_PARAGRAPH.CENTER
r2 = subtitulo.add_run("StudyBro — Red Social Educativa para Estudiantes")
r2.bold = True; r2.font.size = Pt(16)

doc.add_paragraph()
imagen_placeholder("LOGO APP", "Logo de StudyBro")
doc.add_paragraph()

datos = doc.add_paragraph()
datos.alignment = WD_ALIGN_PARAGRAPH.CENTER
datos.add_run(
    "Alumno: Antonio MBA Nzang\n"
    "Ciclo: Desarrollo de Aplicaciones Multiplataforma (DAM) — Grado Superior\n"
    "Tutor: Mario Castro\n"
    "Centro: EPSUM\n"
    "Curso: 2025 / 2026\n"
    "Fecha: Mayo 2026\n"
    "Repositorio: github.com/Antoniomba23/PROYECTO-FINAL-ANDROID-SEGUNDO-DAM (rama antonio)"
).font.size = Pt(11)

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 1. INTRODUCCIÓN
# ══════════════════════════════════════════════════════════════════════════════
h1("1. Introducción y Justificación")
p("StudyBro es una aplicación móvil Android para estudiantes de ciclos formativos y enseñanzas secundarias. Su objetivo es crear un espacio colaborativo donde los alumnos puedan compartir apuntes, publicaciones académicas y valoraciones sobre centros educativos, enriquecido con inteligencia artificial.")
p("La idea surge de una necesidad real: los estudiantes de FP carecen de una plataforma centralizada donde encontrar apuntes de su ciclo, conocer las valoraciones reales de los centros y conectar con compañeros de su misma especialidad.")
p("La aplicación cubre tres grandes necesidades:")
doc.add_paragraph("Descubrimiento de centros: más de 2.000 centros reales de Madrid via API pública.", style='List Bullet')
doc.add_paragraph("Red social académica: muro de publicaciones con comentarios e interacciones.", style='List Bullet')
doc.add_paragraph("Asistente de IA: Gemini 2.5 Flash para resumir apuntes, generar quizzes y mejorar textos.", style='List Bullet')
doc.add_paragraph()

# ══════════════════════════════════════════════════════════════════════════════
# 2. OBJETIVOS
# ══════════════════════════════════════════════════════════════════════════════
h1("2. Objetivos del Proyecto")
h2("2.1 Objetivos Generales")
doc.add_paragraph("Desarrollar una aplicación Android funcional como proyecto final del ciclo DAM.", style='List Bullet')
doc.add_paragraph("Aplicar conocimientos de bases de datos, redes, persistencia, UI y arquitectura.", style='List Bullet')

h2("2.2 Objetivos Específicos")
objetivos = [
    "Implementar autenticación real en la nube con Supabase Auth (JWT).",
    "Crear base de datos local Room (SQLite) con 11 entidades relacionadas.",
    "Consumir API REST pública (Datos Abiertos Madrid) con +2.000 centros educativos.",
    "Integrar la API Gemini de Google para funciones de inteligencia artificial.",
    "Diseñar sistema de roles (Estudiante / Administrador) con acceso diferenciado.",
    "Implementar sincronización entre base de datos local y la nube (Supabase).",
    "Crear interfaz de usuario moderna siguiendo Material Design 3.",
]
for o in objetivos:
    doc.add_paragraph(o, style='List Bullet')
doc.add_paragraph()

# ══════════════════════════════════════════════════════════════════════════════
# 3. TECNOLOGÍAS
# ══════════════════════════════════════════════════════════════════════════════
h1("3. Tecnologías y Herramientas Utilizadas")

h2("3.1 Lenguaje y Plataforma")
tabla(["Tecnología","Versión","Uso"],[
    ["Java","1.8","Lenguaje principal de desarrollo"],
    ["Android SDK","34 (Android 14)","Plataforma de ejecución"],
    ["Android Studio","Hedgehog","Entorno de desarrollo integrado"],
    ["Gradle","8.1.0","Sistema de build y dependencias"],
])

h2("3.2 Persistencia de Datos")
tabla(["Biblioteca","Versión","Uso"],[
    ["Room","2.6.1","ORM para SQLite (base de datos local)"],
    ["Supabase","—","Backend en la nube (PostgreSQL + Auth + Storage)"],
])

h2("3.3 Red y Comunicaciones")
tabla(["Biblioteca","Versión","Uso"],[
    ["Retrofit2","2.9.0","Cliente HTTP para APIs REST"],
    ["OkHttp3","4.12.0","Cliente HTTP de bajo nivel y peticiones IA"],
    ["Gson","—","Serialización/deserialización JSON"],
])

h2("3.4 Interfaz de Usuario")
tabla(["Biblioteca","Versión","Uso"],[
    ["Material Design 3","1.11.0","Componentes visuales modernos"],
    ["ConstraintLayout","2.1.4","Layouts responsivos"],
    ["RecyclerView","—","Listas de desplazamiento eficientes"],
    ["Glide","4.16.0","Carga y caché de imágenes"],
])

h2("3.5 Inteligencia Artificial y APIs Externas")
tabla(["Servicio","Detalle","Uso"],[
    ["Google Gemini API","gemini-2.5-flash","Resumir, quiz, mejorar publicaciones"],
    ["Datos Abiertos Madrid","API REST pública","+2.000 centros educativos reales"],
    ["Supabase Auth","JWT","Autenticación de usuarios"],
    ["Supabase Storage","REST","Almacenamiento de imágenes en la nube"],
])
doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 4. ARQUITECTURA
# ══════════════════════════════════════════════════════════════════════════════
h1("4. Arquitectura del Sistema")
p("StudyBro sigue una arquitectura por capas adaptada al desarrollo Android nativo:")

arq = doc.add_paragraph()
arq.add_run(
    "CAPA DE VISTA\n"
    "  Activities (16) + Layouts XML (26) + Adaptadores RecyclerView (9)\n\n"
    "CAPA DE LÓGICA / UTILIDADES\n"
    "  GeminiHelper · NavigationHelper · SyncHelper · Seguridad\n\n"
    "PERSISTENCIA LOCAL          SERVICIOS REMOTOS\n"
    "  Room (SQLite) v17           Supabase (Auth + Storage)\n"
    "  11 entidades                API Madrid (+2000 centros)\n"
    "                              Gemini AI"
).font.name = 'Courier New'
arq.runs[0].font.size = Pt(9)

doc.add_paragraph()
h2("4.1 Patrón Singleton — Base de Datos")
p("La base de datos Room implementa el patrón Singleton con doble verificación de bloqueo (double-checked locking) para garantizar una única instancia en toda la app:")

codigo = doc.add_paragraph()
codigo.add_run(
    "public static BaseDatosApp getInstance(Context context) {\n"
    "    if (INSTANCE == null) {\n"
    "        synchronized (BaseDatosApp.class) {\n"
    "            if (INSTANCE == null) {\n"
    "                INSTANCE = Room.databaseBuilder(context,\n"
    "                    BaseDatosApp.class, \"studybro-db\")\n"
    "                    .fallbackToDestructiveMigration().build();\n"
    "            }\n"
    "        }\n"
    "    }\n"
    "    return INSTANCE;\n"
    "}"
).font.name = 'Courier New'
codigo.runs[0].font.size = Pt(9)

h2("4.2 Operaciones Asíncronas — ExecutorService")
p("Todas las operaciones de base de datos se ejecutan en hilos de fondo con ExecutorService para evitar bloqueos ANR (Application Not Responding). La actividad principal usa dos executors separados:")
doc.add_paragraph("executorService → Lecturas y sembrado inicial de datos.", style='List Bullet')
doc.add_paragraph("executorEscritura → Escrituras masivas de la API (+2.000 centros).", style='List Bullet')
doc.add_paragraph()

# ══════════════════════════════════════════════════════════════════════════════
# 5. BASE DE DATOS
# ══════════════════════════════════════════════════════════════════════════════
h1("5. Base de Datos — Diagrama Entidad-Relación")
p("La base de datos local (Room/SQLite) es la versión 17 y contiene 11 entidades:")

tabla(["Entidad","Campos principales","Relaciones"],[
    ["USUARIO","id (UUID), nombre, correo, rol, centroId","1:N con Publicacion, Comentario, Valoracion"],
    ["CENTRO","id, nombre, codigoApi, direccion, descripcion, horario","N:M con Especialidad, 1:N con Valoracion"],
    ["ESPECIALIDAD","id, nombre, codigoApi","N:M con Centro, 1:N con Asignatura"],
    ["ASIGNATURA","id, nombre, especialidadId","N:1 con Especialidad"],
    ["PUBLICACION","id, titulo, contenido, asignatura, fecha, estado","N:1 con Usuario y Centro"],
    ["COMENTARIO","id, texto, fecha, usuarioId, publicacionId","N:1 con Publicacion"],
    ["INTERACCION","id, tipo, usuarioId, publicacionId","Likes/dislikes"],
    ["VALORACION_CENTRO","id, puntuacion, comentario, fecha","N:1 con Centro y Usuario"],
    ["CENTRO_ESPECIALIDAD","centroId, especialidadId","Tabla puente N:M"],
    ["SUGERENCIA_ESPECIALIDAD","id, nombre, estado, usuarioId","Propuestas comunidad"],
    ["SUGERENCIA_MATERIA","id, nombre, estado, especialidadId","Propuestas comunidad"],
])
doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 6. MÓDULOS Y FUNCIONALIDADES
# ══════════════════════════════════════════════════════════════════════════════
h1("6. Módulos y Funcionalidades")

h2("6.1 Módulo de Autenticación")
p("Clases: ActividadLogin, ActividadRegistro, ClienteSupabase, ServicioAuth", bold=True)
p("El login se realiza contra Supabase Auth mediante REST. Al autenticarse, el servidor devuelve un JWT que se guarda en SharedPreferences. El flujo es:")
pasos = ["El usuario introduce email y contraseña.",
         "Se envía POST a https://[proyecto].supabase.co/auth/v1/token.",
         "Supabase valida y devuelve token JWT + UUID de usuario.",
         "La app guarda la sesión y sincroniza el usuario con Room.",
         "Si no tiene centro asignado, redirige a ActividadSeleccionarCentro."]
for i,paso in enumerate(pasos,1):
    doc.add_paragraph(f"{i}. {paso}", style='List Number')

imagen_placeholder("LOGIN", "Pantalla de Login — ActividadLogin")
imagen_placeholder("REGISTRO", "Pantalla de Registro — ActividadRegistro")

h2("6.2 Módulo Principal — Buscador de Centros")
p("Clases: ActividadPrincipal, AdaptadorCentros, ServicioApi", bold=True)
p("La pantalla principal muestra +2.000 centros con búsqueda en tiempo real. Técnicas implementadas:")
doc.add_paragraph("Debouncing: el filtro espera 300ms tras cada tecla para reducir operaciones.", style='List Bullet')
doc.add_paragraph("Filterable con normalización: buscar 'informatica' encuentra 'Informática'.", style='List Bullet')
doc.add_paragraph("Carga en background: Room + API en hilo separado, UI siempre reactiva.", style='List Bullet')

imagen_placeholder("PANTALLA PRINCIPAL", "Listado de centros educativos con buscador — ActividadPrincipal")

h2("6.3 Módulo de Publicaciones")
p("Clases: ActividadPublicaciones, ActividadNuevaPublicacion, ActividadDetalle", bold=True)
p("Los estudiantes comparten apuntes y recursos. Cada publicación permite comentarios, interacciones (me gusta) y asistencia de IA. La ActividadDetalle (33 KB) es la más compleja, integrando tres modos de IA.")

imagen_placeholder("NUEVA PUBLICACIÓN", "Formulario de creación de publicación — ActividadNuevaPublicacion")
imagen_placeholder("DETALLE PUBLICACIÓN", "Vista de detalle con comentarios e IA — ActividadDetalle")

h2("6.4 Módulo de Perfil de Estudiante")
p("Clase: ActividadPerfil", bold=True)
p("Muestra datos personales, estadísticas (nº publicaciones, media valoraciones), historial de publicaciones y opción de cambiar foto usando Supabase Storage.")

imagen_placeholder("PERFIL USUARIO", "Perfil del estudiante — ActividadPerfil")

h2("6.5 Módulo de Perfil de Centro")
p("Clase: ActividadPerfilCentro", bold=True)
p("Pantalla con nombre, dirección, horario, accesibilidad, especialidades, valoraciones con estrellas, análisis IA de opiniones y publicaciones del centro.")

imagen_placeholder("PERFIL CENTRO", "Perfil detallado del centro educativo — ActividadPerfilCentro")

h2("6.6 Módulo de Administración")
p("Clases: ActividadPanelAdmin, ActividadNuevoCentro, ActividadGestionUsuarios, ActividadModerarSugerencias", bold=True)
tabla(["Función","Descripción"],[
    ["Gestionar centros","Añadir centros manualmente que no están en la API de Madrid"],
    ["Gestionar usuarios","Ver todos los usuarios registrados y cambiar su estado"],
    ["Moderar sugerencias","Aprobar o rechazar propuestas de nuevas especialidades y materias"],
])

imagen_placeholder("PANEL ADMIN", "Panel de administración — ActividadPanelAdmin")
imagen_placeholder("MODERACIÓN", "Pantalla de moderación de sugerencias — ActividadModerarSugerencias")

h2("6.7 Módulo de Sugerencias")
p("Clases: ActividadSugerirEntidad, ActividadModerarSugerencias", bold=True)
p("Sistema participativo donde los alumnos proponen nuevas especialidades o asignaturas. El administrador las aprueba o rechaza. Las aprobadas quedan disponibles para toda la comunidad.")

imagen_placeholder("SUGERIR", "Formulario de sugerencia de especialidad/materia — ActividadSugerirEntidad")
doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 7. INTEGRACIONES EXTERNAS
# ══════════════════════════════════════════════════════════════════════════════
h1("7. Integraciones Externas")

h2("7.1 API de Datos Abiertos — Ayuntamiento de Madrid")
p("Clases: ClienteApi, ServicioApi, CentroMadrid, RespuestaDatosMadrid", bold=True)
p("La app consume el endpoint público de centros educativos del Ayuntamiento de Madrid (+2.000 registros). El proceso de sincronización:")
for i,paso in enumerate([
    "Se consultan los códigos ya existentes en Room para evitar duplicados.",
    "Solo se procesan centros nuevos.",
    "Inserción masiva en una sola transacción de BD.",
    "Vinculación automática de especialidades por palabras clave en la descripción.",
],1):
    doc.add_paragraph(f"{i}. {paso}", style='List Number')

h2("7.2 Supabase — Backend en la nube")
p("Clases: ClienteSupabase, ServicioAuth, ServicioStorage, SyncHelper", bold=True)
tabla(["Servicio","Uso en StudyBro"],[
    ["Auth","Registro y login con JWT. URL: https://flpdwxgobctdkudovdyx.supabase.co"],
    ["Storage","Fotos de perfil y adjuntos de publicaciones"],
    ["Database","Sincronización de publicaciones y comentarios entre dispositivos"],
])

h2("7.3 Gemini AI — Google Generative AI")
p("Clase: GeminiHelper (396 líneas)", bold=True)
p("Integración con Gemini 2.5 Flash que proporciona 5 funciones de IA:")
tabla(["Función","Método","Descripción"],[
    ["Resumir apuntes","resumirPublicacion()","Resumen didáctico estructurado de la publicación"],
    ["Analizar opiniones","analizarOpiniones()","Síntesis de reseñas de un centro"],
    ["Mejorar publicación","mejorarPublicacion()","Sugiere título y contenido mejorado (JSON)"],
    ["Ayuda general","pedirAyudaGeneral()","Responde preguntas en contexto de la app"],
    ["Quiz automático","pedirAyudaEspecializada(QUIZ)","Genera 3 preguntas de repaso con soluciones"],
])
p("Todas las llamadas son asíncronas (OkHttp enqueue) y los resultados se publican en el hilo principal con Handler(Looper.getMainLooper()).")

imagen_placeholder("IA RESUMEN", "Asistente IA resumiendo una publicación — ActividadDetalle")
doc.add_paragraph()

# ══════════════════════════════════════════════════════════════════════════════
# 8. SISTEMA DE ROLES
# ══════════════════════════════════════════════════════════════════════════════
h1("8. Sistema de Roles y Seguridad")

h2("8.1 Roles")
tabla(["Función","Estudiante","Administrador"],[
    ["Buscar centros","✅","✅"],
    ["Ver publicaciones","✅","✅"],
    ["Crear publicaciones","✅","✅"],
    ["Comentar","✅","✅"],
    ["Valorar centros","✅","✅"],
    ["Sugerir especialidades","✅","✅"],
    ["Usar IA","✅","✅"],
    ["Panel de administrador","❌","✅"],
    ["Crear centros manualmente","❌","✅"],
    ["Gestionar usuarios","❌","✅"],
    ["Moderar sugerencias","❌","✅"],
])

h2("8.2 Gestión de Sesión")
p("El token JWT de Supabase se almacena en SharedPreferences. Al arrancar la app, si existe sesión guardada pero no hay token válido, se limpia automáticamente para forzar nuevo login. El menú lateral (Navigation Drawer) muestra u oculta opciones según el rol.")
doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 9. ESTRUCTURA DEL PROYECTO
# ══════════════════════════════════════════════════════════════════════════════
h1("9. Estructura del Proyecto")

estructura = doc.add_paragraph()
estructura.add_run(
    "android/\n"
    "├── app/src/main/\n"
    "│   ├── AndroidManifest.xml          (16 Activities registradas)\n"
    "│   ├── java/com/dam/studybro/\n"
    "│   │   ├── activities/              (16 pantallas)\n"
    "│   │   ├── adapters/                (9 adaptadores RecyclerView)\n"
    "│   │   ├── database/                (11 entidades + 11 DAOs + Seeder)\n"
    "│   │   ├── modelos/                 (modelos API Madrid para Gson)\n"
    "│   │   ├── network/                 (Supabase API Retrofit)\n"
    "│   │   ├── red/                     (API Madrid Retrofit)\n"
    "│   │   ├── supabase/                (Auth y Storage cliente)\n"
    "│   │   └── utils/\n"
    "│   │       ├── GeminiHelper.java    (IA — 5 funciones, 396 líneas)\n"
    "│   │       ├── NavigationHelper.java\n"
    "│   │       ├── SyncHelper.java\n"
    "│   │       └── Seguridad.java\n"
    "│   └── res/\n"
    "│       ├── layout/                  (26 layouts XML)\n"
    "│       └── values/                  (colors, strings, themes, dimens)\n"
    "└── build.gradle                     (Room, Retrofit, Glide, Material)"
).font.name = 'Courier New'
estructura.runs[0].font.size = Pt(9)
doc.add_paragraph()

# ══════════════════════════════════════════════════════════════════════════════
# 10. CONCLUSIONES
# ══════════════════════════════════════════════════════════════════════════════
h1("10. Conclusiones y Trabajo Futuro")

h2("10.1 Conclusiones")
p("El desarrollo de StudyBro ha permitido aplicar de forma integrada los contenidos del ciclo DAM en un proyecto real:")
for c in [
    "Programación Java orientada a objetos con patrones de diseño (Singleton, Callback, Adapter).",
    "Bases de datos relacionales con Room/SQLite: relaciones 1:N y N:M.",
    "Programación de servicios con Retrofit para consumo de múltiples APIs REST.",
    "Multithreading con ExecutorService para operaciones asíncronas sin ANR.",
    "Interfaz de usuario avanzada con Navigation Drawer, BottomNavigation, RecyclerView y Material Design 3.",
    "Integración con servicios en la nube mediante Supabase (JWT, storage, sync).",
    "Inteligencia Artificial aplicada a un caso de uso real con la API de Gemini.",
]:
    doc.add_paragraph(c, style='List Bullet')

h2("10.2 Trabajo Futuro")
for tf in [
    "Notificaciones push cuando alguien comenta una publicación.",
    "Sistema de mensajería privada entre estudiantes del mismo centro.",
    "Ampliar cobertura a centros de otras comunidades autónomas.",
    "Publicación en Google Play Store.",
    "Migrar UI a Jetpack Compose.",
    "Soporte offline completo con sincronización diferida.",
]:
    doc.add_paragraph(tf, style='List Bullet')

doc.add_paragraph()
final = doc.add_paragraph()
final.alignment = WD_ALIGN_PARAGRAPH.CENTER
final.add_run("Fin de la Memoria Técnica — StudyBro v1.0\n"
              "Antonio MBA Nzang — DAM Segundo Curso — 2026").font.italic = True

# ── Guardar ───────────────────────────────────────────────────────────────────
ruta = r"d:\DAM\Rutas y Proyectos\Proyectos\DAM Proyecto Final\MEMORIA_TECNICA_STUDYBRO.docx"
doc.save(ruta)
print(f"OK - Documento guardado en: {ruta}")
