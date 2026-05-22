# -*- coding: utf-8 -*-
import os
from docx import Document
from docx.shared import Pt, RGBColor, Inches, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

# ── 1. Inicialización y márgenes clásicos de Word ──
doc = Document()
for section in doc.sections:
    section.top_margin    = Cm(2.5)
    section.bottom_margin = Cm(2.5)
    section.left_margin   = Cm(3.0)
    section.right_margin  = Cm(2.5)

# ── 2. Helpers para dar un estilo limpio y profesional al Word ──
def h1(texto):
    p = doc.add_heading(texto, level=1)
    p.paragraph_format.space_before = Pt(18)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    run = p.runs[0]
    run.font.name = 'Arial'
    run.font.size = Pt(14)
    run.font.color.rgb = RGBColor(0x22, 0x33, 0x55) # Azul oscuro clásico
    return p

def h2(texto):
    p = doc.add_heading(texto, level=2)
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    run = p.runs[0]
    run.font.name = 'Arial'
    run.font.size = Pt(12)
    run.font.color.rgb = RGBColor(0x33, 0x55, 0x77) # Azul intermedio
    return p

def h3(texto):
    p = doc.add_heading(texto, level=3)
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.keep_with_next = True
    run = p.runs[0]
    run.font.name = 'Arial'
    run.font.size = Pt(11)
    run.font.color.rgb = RGBColor(0x44, 0x44, 0x44)
    return p

def p(texto, bold=False, italic=False, size=11, space_after=6):
    para = doc.add_paragraph()
    para.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    para.paragraph_format.space_after = Pt(space_after)
    para.paragraph_format.line_spacing = 1.15
    run = para.add_run(texto)
    run.font.name = 'Calibri'
    run.font.size = Pt(size)
    run.bold = bold
    run.italic = italic
    return para

def codigo_bloque(texto):
    para = doc.add_paragraph()
    para.paragraph_format.left_indent = Cm(0.8)
    para.paragraph_format.space_before = Pt(4)
    para.paragraph_format.space_after = Pt(4)
    para.paragraph_format.line_spacing = 1.0
    
    run = para.add_run(texto)
    run.font.name = 'Courier New'
    run.font.size = Pt(9.0)
    run.font.color.rgb = RGBColor(0x11, 0x11, 0x11)
    
    tcPr = para._p.get_or_add_pPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:fill'), 'F5F6F8')
    shd.set(qn('w:val'), 'clear')
    tcPr.append(shd)
    return para

def tabla(headers, rows):
    t = doc.add_table(rows=1+len(rows), cols=len(headers))
    t.style = 'Table Grid'
    hdr = t.rows[0].cells
    for i, h in enumerate(headers):
        hdr[i].text = h
        hdr[i].paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        for run in hdr[i].paragraphs[0].runs:
            run.bold = True
            run.font.name = 'Arial'
            run.font.size = Pt(9.5)
            run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        
        tcPr = hdr[i]._tc.get_or_add_tcPr()
        shd = OxmlElement('w:shd')
        shd.set(qn('w:fill'), '223355') # Fondo azul oscuro
        shd.set(qn('w:color'), 'auto')
        shd.set(qn('w:val'), 'clear')
        tcPr.append(shd)
        
    for ri, row in enumerate(rows):
        cells = t.rows[ri+1].cells
        for ci, val in enumerate(row):
            cells[ci].text = val
            cells[ci].paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.LEFT
            for run in cells[ci].paragraphs[0].runs:
                run.font.name = 'Calibri'
                run.font.size = Pt(9.5)
    doc.add_paragraph()

def caja_imagen_placeholder(id_captura, descripcion_captura):
    """Genera un recuadro de instrucción claro para que el alumno pegue la captura en Word."""
    para = doc.add_paragraph()
    para.paragraph_format.left_indent = Cm(0.8)
    para.paragraph_format.right_indent = Cm(0.8)
    para.paragraph_format.space_before = Pt(8)
    para.paragraph_format.space_after = Pt(8)
    
    # Sombreado gris para destacar
    tcPr = para._p.get_or_add_pPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:fill'), 'F0F4F8')
    shd.set(qn('w:val'), 'clear')
    tcPr.append(shd)
    
    run = para.add_run(
        f"📸 [PEGA AQUÍ TU CAPTURA DE PANTALLA: {id_captura}]\n"
        f"Qué debe verse: {descripcion_captura}\n"
        f"-> (En Word: Pulsa sobre esta línea, ve al menú 'Insertar' -> 'Imágenes' y selecciona tu captura)."
    )
    run.bold = True
    run.font.name = 'Arial'
    run.font.size = Pt(9.5)
    run.font.color.rgb = RGBColor(0x33, 0x55, 0x77)
    doc.add_paragraph()

# ══════════════════════════════════════════════════════════════════════════════
# PORTADA (ESTILO ESTUDIANTE DE DAM)
# ══════════════════════════════════════════════════════════════════════════════
for _ in range(4): doc.add_paragraph()

titulo = doc.add_paragraph()
titulo.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = titulo.add_run("MEMORIA TÉCNICA DEL PROYECTO DE FIN DE CICLO")
r.bold = True
r.font.name = 'Arial'
r.font.size = Pt(18)
r.font.color.rgb = RGBColor(0x22, 0x33, 0x55)

subtitulo = doc.add_paragraph()
subtitulo.alignment = WD_ALIGN_PARAGRAPH.CENTER
r2 = subtitulo.add_run("StudyFiles: Aplicación para compartir apuntes y gestionar archivos de clase en Android")
r2.bold = True
r2.font.name = 'Arial'
r2.font.size = Pt(13)
r2.font.color.rgb = RGBColor(0x55, 0x55, 0x55)

for _ in range(2): doc.add_paragraph()

caja_imagen_placeholder("LOGO_APP.png", "El icono de la aplicación o una imagen bonita de la pantalla principal.")

for _ in range(2): doc.add_paragraph()

datos = doc.add_paragraph()
datos.alignment = WD_ALIGN_PARAGRAPH.CENTER
datos.paragraph_format.line_spacing = 1.3
r_datos = datos.add_run(
    "Autor: Antonio MBA Nzang\n"
    "Ciclo: Desarrollo de Aplicaciones Multiplataforma (DAM)\n"
    "Centro educativo: EPSUM\n"
    "Tutor del proyecto: Mario Castro\n"
    "Curso y grupo: 2.º DAM — Mayo de 2026\n"
)
r_datos.font.name = 'Arial'
r_datos.font.size = Pt(10.5)

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# PROPUESTA DE ÍNDICE PARA LA DEFENSA
# ══════════════════════════════════════════════════════════════════════════════
h1("Índice de Contenidos del Proyecto")
p("He organizado la memoria en los siguientes apartados para que te sirva también como guion al hacer la presentación del proyecto ante el tribunal:", italic=True)

def item_indice(numero, seccion, indent=0.0, bold=False):
    para = doc.add_paragraph()
    para.paragraph_format.left_indent = Cm(indent)
    para.paragraph_format.space_after = Pt(2)
    run = para.add_run(f"{numero} {seccion}")
    run.font.name = 'Calibri'
    run.font.size = Pt(11)
    run.bold = bold

item_indice("1.", "INTRODUCCIÓN Y POR QUÉ HICE ESTA APP", bold=True)
item_indice("1.1.", "El problema de los apuntes perdidos al final del curso", indent=0.5)
item_indice("1.2.", "La solución que propongo con StudyFiles", indent=0.5)

item_indice("2.", "OBJETIVO Y LO QUE BUSCO CONSEGUIR", bold=True)
item_indice("2.1.", "Objetivo de la aplicación", indent=0.5)
item_indice("2.2.", "Funcionalidades que he implementado", indent=0.5)

item_indice("3.", "CÓMO ESTÁ ORGANIZADA LA APLICACIÓN (MÓDULOS)", bold=True)
item_indice("3.1.", "Módulo de Login y Registro de usuarios", indent=0.5)
item_indice("3.2.", "Buscador de apuntes públicos por asignaturas", indent=0.5)
item_indice("3.3.", "Pantalla de detalle del archivo: descargas, comentarios y me gusta", indent=0.5)
item_indice("3.4.", "Mi Nube: Espacio personal y carpetas privadas en la nube", indent=0.5)
item_indice("3.5.", "El Tutor Virtual: Chat interactivo de ayuda al estudio", indent=0.5)

item_indice("4.", "CASOS DE USO (EJEMPLOS DE CÓMO SE USA LA APP)", bold=True)
item_indice("4.1.", "Caso de Uso 1: Crear una cuenta e iniciar sesión", indent=0.5)
item_indice("4.2.", "Caso de Uso 2: Buscar y descargar un apunte público", indent=0.5)
item_indice("4.3.", "Caso de Uso 3: Subir un PDF privado a 'Mi Nube'", indent=0.5)
item_indice("4.4.", "Caso de Uso 4: Preguntar una duda al Tutor", indent=0.5)

item_indice("5.", "CÓMO HE ESTRUCTURADO EL CÓDIGO EN ANDROID STUDIO", bold=True)
item_indice("5.1.", "Las carpetas del proyecto en Java", indent=0.5)
item_indice("5.2.", "Para qué sirve cada paquete de código", indent=0.5)

item_indice("6.", "PERSISTENCIA LOCAL CON ROOM (FAVORITOS Y VOTACIONES)", bold=True)
item_indice("6.1.", "Cómo he montado la base de datos Room local", indent=0.5)
item_indice("6.2.", "Lógica de favoritos y evitar votos repetidos", indent=0.5)

item_indice("7.", "CONEXIÓN A INTERNET Y BASES DE DATOS EN LA NUBE (APIS)", bold=True)
item_indice("7.1.", "Cómo nos comunicamos con Supabase usando Retrofit", indent=0.5)
item_indice("7.2.", "Cómo funciona la llamada del Tutor Virtual", indent=0.5)

item_indice("8.", "ASIGNATURAS DE DAM APLICADAS Y ERRORES DE DESARROLLO", bold=True)
item_indice("8.1.", "Qué asignaturas del ciclo me han servido para la app", indent=0.5)
item_indice("8.2.", "Problemas difíciles que tuve y cómo los solucioné", indent=0.5)

item_indice("9.", "CONCLUSIÓN DEL PROYECTO Y FUTURAS MEJORAS", bold=True)

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 1. INTRODUCCIÓN Y POR QUÉ HICE ESTA APP
# ══════════════════════════════════════════════════════════════════════════════
h1("1. Introducción y por qué hice esta app")

h2("1.1. El problema de los apuntes perdidos al final del curso")
p("Cuando estás estudiando un ciclo formativo o bachillerato, te das cuenta de que la organización de los apuntes siempre es un lío. Los estudiantes que van bien en clase crean resúmenes excelentes, guías de estudio y listas de ejercicios resueltos. Sin embargo, cuando llega el verano y aprueban las asignaturas, la mayoría de esos archivos se quedan guardados en sus ordenadores personales o se borran de los chats de WhatsApp.")
p("Al año siguiente, los nuevos alumnos que empiezan el curso tienen que volver a redactar apuntes y buscar información desde cero porque no tienen forma de acceder a lo que hicieron sus compañeros del año anterior. Esto hace que se pierda muchísimo material valioso de un curso para otro.")

h2("1.2. La solución que propongo con StudyFiles")
p("Para solucionar esto, he creado StudyFiles. Es una aplicación móvil pensada para instalar en el teléfono y que funciona como un espacio donde los estudiantes pueden compartir sus apuntes públicamente. Lo bueno es que todo está organizado por categorías de asignaturas para que sea facilísimo de encontrar.")
p("La aplicación también ayuda en el día a día del estudiante porque tiene una parte privada para subir tus propios archivos a tu carpeta ('Mi Nube') y un chat con un tutor virtual que te explica conceptos o te hace resúmenes de los temas que no entiendes sin tener que salir del teléfono.")

# ══════════════════════════════════════════════════════════════════════════════
# 2. OBJETIVO Y LO QUE BUSCO CONSEGUIR
# ══════════════════════════════════════════════════════════════════════════════
h1("2. Objetivo y lo que busco conseguir")

h2("2.1. Objetivo de la aplicación")
p("El objetivo principal que tenía cuando empecé a desarrollar StudyFiles era hacer una herramienta que fuese realmente práctica para los estudiantes. Quería que fuese rápida, que no tuviera una interfaz complicada y que se pudiera usar en cualquier móvil Android.")

h2("2.2. Funcionalidades que he implementado")
p("Para que la aplicación sea completa, he desarrollado estas características principales:")
doc.add_paragraph("Registro e inicio de sesión seguro, para que cada alumno tenga sus propios apuntes y configuración guardada.", style='List Bullet')
doc.add_paragraph("Buscador de apuntes en tiempo real, para escribir una palabra clave y encontrar los documentos al instante.", style='List Bullet')
doc.add_paragraph("Sección de comentarios y votos, para que la propia comunidad diga si un archivo es útil y sirva para resolver dudas sobre el documento.", style='List Bullet')
doc.add_paragraph("Mi Nube: un espacio de almacenamiento privado donde puedes crear carpetas y guardar tus propios documentos personales.", style='List Bullet')
doc.add_paragraph("Tutor de consulta: un chat interactivo que te ayuda con dudas escolares o a estudiar mejor.", style='List Bullet')

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 3. CÓMO ESTÁ ORGANIZADA LA APLICACIÓN (MÓDULOS)
# ══════════════════════════════════════════════════════════════════════════════
h1("3. Cómo está organizada la aplicación (Módulos)")
p("He dividido el desarrollo de la aplicación en varios módulos funcionales que controlo mediante pantallas diferentes. A continuación los explico uno a uno de forma sencilla:")

h2("3.1. Módulo de Login y Registro de usuarios")
p("Esta es la puerta de entrada a la aplicación. Sirve para que nadie pueda ver los apuntes privados de los alumnos sin estar registrado. He usado un sistema que guarda la sesión en el teléfono, por lo que una vez que haces login ya no hace falta volver a escribir tu usuario y contraseña la próxima vez que abras la app.")
caja_imagen_placeholder("PANTALLA_LOGIN.png", "Pantalla de Login con los campos para meter el correo y la contraseña.")

h2("3.2. Buscador de apuntes públicos por asignaturas")
p("Esta es la pantalla de inicio de la aplicación. Muestra una cuadrícula con las diferentes categorías (Informática, Matemáticas, Historia, Lengua, Ciencias, etc.). Al pulsar en una, se abre una lista con todos los apuntes públicos de esa asignatura. He programado un buscador en la parte de arriba: según vas escribiendo letras, la lista se va actualizando en tiempo real sin tener que pulsar ningún botón de buscar.")
caja_imagen_placeholder("PANTALLA_PRINCIPAL.png", "Pantalla de inicio con las asignaturas de la app y el botón del tutor.")

h2("3.3. Pantalla de detalle: descargas, comentarios y me gusta")
p("Al pulsar sobre un apunte de la lista, se abre esta pantalla que muestra toda su información. Desde aquí se pueden hacer tres cosas importantes:")
doc.add_paragraph("Descargar: El archivo se baja directamente a la carpeta de descargas del móvil para poder leerlo.", style='List Bullet')
doc.add_paragraph("Votar utilidad: Puedes darle a un botón para indicar si te ha parecido útil. Si ya le habías dado, se quita al volver a pulsar.", style='List Bullet')
doc.add_paragraph("Comentarios: Un foro abajo del archivo para que los estudiantes escriban dudas o den las gracias. He programado la opción de que cada usuario pueda borrar sus propios comentarios si quiere.", style='List Bullet')
caja_imagen_placeholder("PANTALLA_DETALLE.png", "Pantalla detallada de un archivo con sus comentarios y los botones de voto y descarga.")

h2("3.4. Mi Nube: Espacio personal y carpetas privadas")
p("Esta es una de las partes más útiles de la app. Es como una nube privada de almacenamiento (estilo Drive) para el estudiante. Le permite crear carpetas con el nombre que quiera y subir sus propios archivos. Estos documentos son completamente privados y ningún otro usuario de la aplicación puede verlos.")
caja_imagen_placeholder("PANTALLA_NUBE.png", "Vista de Mi Nube mostrando las carpetas creadas y los archivos guardados en ellas.")

h2("3.5. El Tutor Virtual: Chat interactivo de ayuda al estudio")
p("Es una ventana de chat donde el estudiante puede hablar con un tutor automatizado. He configurado al tutor para que responda dudas basándose en los temas de estudio y las asignaturas que se manejan en la aplicación, por lo que da respuestas muy centradas en lo que el alumno necesita en ese momento.")
caja_imagen_placeholder("PANTALLA_CHAT.png", "El chat interactivo con el tutor resolviendo una duda académica.")

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 4. CASOS DE USO (EJEMPLOS DE CÓMO SE USA LA APP)
# ══════════════════════════════════════════════════════════════════════════════
h1("4. Casos de Uso (Ejemplos de cómo se usa la app)")
p("Para explicar de forma práctica al tribunal cómo funciona la lógica interna, he detallado los pasos que sigue un usuario al interactuar con las partes más importantes de la aplicación:")

h2("4.1. Caso de Uso 1: Crear una cuenta e iniciar sesión")
p("1. El estudiante abre StudyFiles y ve el formulario de login.", space_after=2)
p("2. Pulsa en 'Registrarse', introduce su correo y la contraseña que quiere usar, y confirma.", space_after=2)
p("3. Tras registrarse con éxito, vuelve a la pantalla anterior, escribe sus datos y pulsa en 'Entrar'.", space_after=2)
p("4. La aplicación comprueba los datos a través de Internet, guarda la sesión y le da paso al muro de asignaturas.", space_after=6)

h2("4.2. Caso de Uso 2: Buscar y descargar un apunte público")
p("1. En la pantalla principal, el estudiante pulsa sobre la categoría 'Informática'.", space_after=2)
p("2. En el buscador escribe la palabra 'Java' para filtrar los apuntes.", space_after=2)
p("3. Toca sobre el apunte que le interesa (por ejemplo: 'Apuntes examen Java básico').", space_after=2)
p("4. Se abre el detalle del archivo y el usuario pulsa en el botón con el icono de descarga.", space_after=2)
p("5. El móvil le pide permiso de almacenamiento, descarga el PDF y lo guarda en el teléfono para poder leerlo.", space_after=6)

h2("4.3. Caso de Uso 3: Subir un PDF privado a 'Mi Nube'")
p("1. El estudiante pulsa en la pestaña de 'Mi Nube' en la barra inferior de la pantalla principal.", space_after=2)
p("2. Pulsa sobre el botón para crear una carpeta y la llama 'Temas de examen'.", space_after=2)
p("3. Entra dentro de la carpeta y toca el botón flotante con el icono de subir.", space_after=2)
p("4. Selecciona un archivo PDF de su teléfono.", space_after=2)
p("5. La app sube el archivo a internet de forma privada y lo muestra dentro de la carpeta del estudiante.", space_after=6)
caja_imagen_placeholder("PANTALLA_SUBIR.png", "Pantalla de subida de apuntes mostrando el formulario y el selector de archivos del móvil.")

h2("4.4. Caso de Uso 4: Preguntar una duda al Tutor")
p("1. El estudiante pulsa sobre el botón flotante del Tutor en la pantalla de inicio.", space_after=2)
p("2. Escribe en el chat: '¿Me puedes hacer un resumen de los bucles en programación?'.", space_after=2)
p("3. Pulsa enviar y aparece un indicador de carga mientras el tutor procesa la pregunta.", space_after=2)
p("4. El Tutor le responde con una explicación sencilla y un par de ejemplos de código.", space_after=2)
p("5. El chat se actualiza y la conversación queda guardada en el historial.", space_after=6)

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 5. CÓMO HE ESTRUCTURADO EL CÓDIGO EN ANDROID STUDIO
# ══════════════════════════════════════════════════════════════════════════════
h1("5. Cómo he estructurado el código en Android Studio")
p("Cuando creas una aplicación en Android Studio, es muy importante tener los archivos bien organizados por carpetas (paquetes) para no volverte loco buscando el código. En mi caso, he organizado el proyecto en cinco carpetas principales dentro de la ruta del paquete de la aplicación:", space_after=8)

# Estructura del árbol de paquetes simplificada
codigo_bloque(
    "com.dam.studyfiles/\n"
    "│\n"
    "├── activities/      <-- El código de las pantallas (Java)\n"
    "│\n"
    "├── adapters/        <-- Los adaptadores para mostrar las listas de datos\n"
    "│\n"
    "├── database/        <-- Base de datos Room para guardar datos en el móvil\n"
    "│\n"
    "├── modelos/         <-- Clases simples que definen cómo es un archivo, carpeta, etc.\n"
    "│\n"
    "├── network/         <-- Lógica para conectar la app a Internet\n"
    "│\n"
    "└── utils/           <-- Código de ayuda (Tutor virtual, utilidades de red)"
)

h2("5.1. Qué hace cada carpeta de código")
p("A continuación explico para qué sirve cada una de estas carpetas de forma muy clara:", space_after=10)

h3("Carpeta 'activities'")
p("Aquí guardo todas las clases Java que controlan lo que se ve en la pantalla del teléfono. Cada pantalla de la aplicación tiene su propio archivo de código (por ejemplo, ActividadLogin.java, ActividadChat.java o ActividadDetalle.java). Estas clases se encargan de capturar cuando el usuario pulsa un botón, escribir textos en pantalla y cambiar de una ventana a otra.")

h3("Carpeta 'adapters'")
p("Esta carpeta es clave. En Android, cuando quieres mostrar una lista de elementos que se pueden deslizar (como la lista de apuntes o los comentarios), se usa un componente visual llamado RecyclerView. El adaptador es el código que hace de 'puente': coge la lista de datos en Java y los va inyectando en las tarjetas visuales de la pantalla uno por uno.")

h3("Carpeta 'database'")
p("Aquí está todo el código relacionado con Room, que es la base de datos interna SQLite del teléfono. Contiene la clase que crea la base de datos local y los archivos DAO, que son interfaces donde defino las consultas SQL que voy a usar (como insertar favoritos, borrar favoritos o listarlos).")

h3("Carpeta 'modelos'")
p("Son clases Java normales y corrientes (POJOs) que sirven para estructurar la información. Por ejemplo, la clase Comentario.java solo tiene variables como id, contenido y fecha, además de sus métodos get y set. Nos sirven para mover la información fácilmente dentro del código.")

h3("Carpeta 'network'")
p("Contiene la configuración de Retrofit, que es la librería que utilizo para hacer las conexiones a Internet. Aquí defino los servicios web, es decir, las URLs a las que la aplicación tiene que conectarse para subir archivos, borrar comentarios o descargar la lista de apuntes públicos.")

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 6. PERSISTENCIA LOCAL CON ROOM (FAVORITOS Y VOTACIONES)
# ══════════════════════════════════════════════════════════════════════════════
h1("6. Persistencia local con Room (Favoritos y Votaciones)")

h2("6.1. Cómo he montado la base de datos Room local")
p("Para que la aplicación no tenga que estar descargando de internet todo el rato los apuntes que más le gustan al estudiante, he programado una base de datos local usando Room. Room funciona sobre SQLite, que es un motor de base de datos muy ligero integrado en Android. He implementado esta base de datos usando el patrón Singleton para asegurar que no se creen copias repetidas en la memoria del teléfono.")
p("Código de la clase principal de la base de datos (BaseDatos.java):")
codigo_bloque(
    "@Database(entities = { Favorito.class, VotoLocal.class }, version = 3, exportSchema = false)\n"
    "public abstract class BaseDatos extends RoomDatabase {\n"
    "    private static BaseDatos INSTANCE;\n"
    "\n"
    "    public abstract FavoritoDao favoritoDao();\n"
    "    public abstract VotoLocalDao votoLocalDao();\n"
    "\n"
    "    public static synchronized BaseDatos getInstance(Context context) {\n"
    "        if (INSTANCE == null) {\n"
    "            INSTANCE = Room.databaseBuilder(\n"
    "                    context.getApplicationContext(),\n"
    "                    BaseDatos.class,\n"
    "                    \"studyfiles-db\"\n"
    "            ).fallbackToDestructiveMigration().build();\n"
    "        }\n"
    "        return INSTANCE;\n"
    "    }\n"
    "}"
)

h2("6.2. Lógica de favoritos y evitar votos repetidos")
p("La base de datos tiene dos tablas locales:")
doc.add_paragraph("Favorito: Guarda los datos de los apuntes que el usuario ha marcado con una estrella. Esto permite que el estudiante pueda verlos al instante en su pestaña de favoritos sin conexión a internet.", style='List Bullet')
doc.add_paragraph("VotoLocal: Esta tabla sirve para llevar el control de los archivos que el usuario ya ha votado. Así, cuando el estudiante abre un apunte, la app mira en esta tabla local para saber si ya le dio 'me gusta' y desactivar o activar el botón de forma correcta, evitando votos falsos repetidos en el servidor remoto.", style='List Bullet')
caja_imagen_placeholder("PANTALLA_FAVORITOS.png", "Pestaña de favoritos del alumno mostrando la lista guardada en la base de datos local.")

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 7. CONEXIÓN A INTERNET Y BASES DE DATOS EN LA NUBE (APIS)
# ══════════════════════════════════════════════════════════════════════════════
h1("7. Conexión a Internet y bases de datos en la Nube (APIs)")

h2("7.1. Cómo nos comunicamos con Supabase usando Retrofit")
p("Para que todos los estudiantes puedan compartir sus apuntes públicamente, la aplicación tiene que subir y bajar la información de internet. Para esto utilizo Supabase como base de datos en la nube y Retrofit como el cliente en Android para hacer las llamadas de red.")
p("Al iniciar sesión, el servidor nos devuelve un token de seguridad (JWT). He programado la aplicación para que inyecte este token automáticamente en cada petición que hace a internet. De esta manera, el servidor web sabe exactamente qué alumno está escribiendo un comentario o subiendo un archivo, bloqueando el acceso a usuarios no identificados.")
p("Código de la interfaz de llamadas web (SupabaseApi.java):")
codigo_bloque(
    "public interface SupabaseApi {\n"
    "    @GET(\"archivos\")\n"
    "    Call<List<Archivo>> buscarArchivosAvanzado(@Query(\"or\") String orQuery);\n"
    "\n"
    "    @POST(\"comentarios\")\n"
    "    Call<List<Comentario>> crearComentario(@Body Comentario request);\n"
    "\n"
    "    @DELETE(\"comentarios\")\n"
    "    Call<Void> eliminarComentario(@Query(\"id\") String idComentario);\n"
    "}"
)

h2("7.2. Cómo funciona la llamada del Tutor Virtual")
p("El chat con el Tutor Virtual está controlado por la clase GeminiChatHelper.java. Cuando el estudiante envía un mensaje en el chat, no se envía la pregunta tal cual; primero, el código busca palabras clave en los archivos públicos del estudiante y añade esa información como contexto a la pregunta. Luego, realiza una petición de tipo POST a la API utilizando la librería OkHttp. Cuando el servidor nos responde con el texto traducido o explicado, utilizamos un Handler para actualizar la interfaz del chat de forma segura sin que la app falle.")

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 8. ASIGNATURAS DE DAM APLICADAS Y ERRORES DE DESARROLLO
# ══════════════════════════════════════════════════════════════════════════════
h1("8. Asignaturas de DAM aplicadas y errores de desarrollo")

h2("8.1. Qué asignaturas del ciclo me han servido para la app")
p("Desarrollar este proyecto ha sido la oportunidad perfecta para aplicar todo lo que he aprendido en las clases de DAM durante estos dos años. Las asignaturas que más me han servido son:")
doc.add_paragraph("Programación Multimedia y Dispositivos Móviles: Para entender el funcionamiento de las actividades en Android, cómo manejar los permisos del teléfono y cómo usar hilos de ejecución en segundo plano para que la app no se congele al conectar con internet.", style='List Bullet')
doc.add_paragraph("Acceso a Datos: Para diseñar las tablas SQLite locales de favoritos y realizar consultas de inserción y borrado a través de Room.", style='List Bullet')
doc.add_paragraph("Desarrollo de Interfaces: Para maquetar todas las vistas de la app en XML con Material Design 3, y conseguir que se vean bien tanto en pantallas de teléfonos pequeños como en tablets.", style='List Bullet')
doc.add_paragraph("Programación de Servicios y Procesos: Para hacer las conexiones con los servidores web y las llamadas asíncronas de red usando Retrofit.", style='List Bullet')

h2("8.2. Problemas difíciles que tuve y cómo los solucioné")
p("Como programador novato, durante la creación de la app me encontré con varios problemas que me llevaron bastante tiempo de investigación:")
p("1. El error de claves foráneas al comentar apuntes: Al principio del desarrollo, al intentar poner un comentario en un archivo, la aplicación me devolvía un error de base de datos en internet (error 409). Al investigar la base de datos, me di cuenta de que la tabla remota de comentarios estaba mal enlazada: intentaba asociarse a una tabla que no existía. Lo solucioné modificando la relación en la nube para que se enlazara directamente con la ID de los archivos. Además, en el código Java añadí el prefijo 'user_' para que las IDs de los usuarios coincidieran y la app les dejara borrar sus propios comentarios.", bold=True)
p("2. El error de lectura de archivos en Android Studio: Cuando intenté hacer el formulario de subida de archivos, utilicé un método de lectura de bytes muy moderno. Al compilar el proyecto, Gradle daba errores porque no era compatible con las versiones de Android anteriores. Tuve que reescribir la lógica usando los flujos de lectura de bytes clásicos de Java (InputStream y ByteArrayOutputStream), que son totalmente compatibles con Java 8 y garantizan que la app funcione sin romperse en teléfonos antiguos.", bold=True)

# ══════════════════════════════════════════════════════════════════════════════
# 9. CONCLUSIÓN DEL PROYECTO Y FUTURAS MEJORAS
# ══════════════════════════════════════════════════════════════════════════════
h1("9. Conclusión del proyecto y futuras mejoras")
p("Creo que StudyFiles ha quedado muy bien y funciona de forma estable. Es una aplicación rápida y útil que soluciona un problema real en los centros de estudio. De cara al futuro, la aplicación está pensada para poder escalar de forma sencilla, por lo que me gustaría añadir en futuras versiones:")
doc.add_paragraph("Notificaciones push para que el móvil te avise en tiempo real cuando alguien comente un apunte tuyo o le dé un voto de utilidad.", style='List Bullet')
doc.add_paragraph("Mejorar las capacidades del Tutor Virtual para que pueda leer archivos PDF completos subidos por el usuario y crear resúmenes automáticos al instante.", style='List Bullet')

doc.add_paragraph()
final = doc.add_paragraph()
final.alignment = WD_ALIGN_PARAGRAPH.CENTER
final.paragraph_format.space_before = Pt(25)
r_fin = final.add_run("Fin de la memoria técnica de StudyFiles\n"
                      "Redactado por Antonio MBA Nzang — DAM 2026")
r_fin.italic = True
r_fin.font.size = Pt(10.0)

# ── 4. Guardar Documento ──
ruta_final = r"d:\DAM\Rutas y Proyectos\Proyectos\DAM Proyecto Final\MEMORIA_TECNICA_STUDYBRO.docx"
try:
    doc.save(ruta_final)
    print(f"OK - Documento de TFG guardado correctamente en: {ruta_final}")
except PermissionError:
    ruta_alt = r"d:\DAM\Rutas y Proyectos\Proyectos\DAM Proyecto Final\MEMORIA_TECNICA_STUDYBRO_HUMANA.docx"
    doc.save(ruta_alt)
    print(f"ATENCION - Guardado alternativamente en: {ruta_alt}")
