# -*- coding: utf-8 -*-
from docx import Document
from docx.shared import Pt, RGBColor, Inches, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
import os
import shutil

# ── 1. Inicialización y Configuración de Márgenes ──
doc = Document()
for section in doc.sections:
    section.top_margin    = Cm(2.5)
    section.bottom_margin = Cm(2.5)
    section.left_margin   = Cm(3.0)
    section.right_margin  = Cm(2.5)

# ── 2. Helpers para Estilos Académicos ──
def h1(texto):
    p = doc.add_heading(texto, level=1)
    p.paragraph_format.space_before = Pt(18)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    run = p.runs[0]
    run.font.name = 'Arial'
    run.font.size = Pt(16)
    run.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D) # Azul Marino Institucional
    return p

def h2(texto):
    p = doc.add_heading(texto, level=2)
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    run = p.runs[0]
    run.font.name = 'Arial'
    run.font.size = Pt(13)
    run.font.color.rgb = RGBColor(0x2E, 0x5B, 0x88) # Azul secundario
    return p

def h3(texto):
    p = doc.add_heading(texto, level=3)
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.keep_with_next = True
    run = p.runs[0]
    run.font.name = 'Arial'
    run.font.size = Pt(11.5)
    run.font.color.rgb = RGBColor(0x55, 0x55, 0x55)
    return p

def p(texto, bold=False, italic=False, size=11, space_after=6, align=WD_ALIGN_PARAGRAPH.JUSTIFY):
    para = doc.add_paragraph()
    para.alignment = align
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
    para.paragraph_format.left_indent = Cm(1.0)
    para.paragraph_format.space_before = Pt(4)
    para.paragraph_format.space_after = Pt(4)
    para.paragraph_format.line_spacing = 1.0
    
    run = para.add_run(texto)
    run.font.name = 'Courier New'
    run.font.size = Pt(9.0)
    run.font.color.rgb = RGBColor(0x22, 0x22, 0x22)
    
    # Añadir un sombreado gris claro de fondo
    tcPr = para._p.get_or_add_pPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:fill'), 'F4F5F7')
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
            run.font.size = Pt(10)
            run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        
        tcPr = hdr[i]._tc.get_or_add_tcPr()
        shd = OxmlElement('w:shd')
        shd.set(qn('w:fill'), '1B365D') # Fondo azul marino
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
                run.font.size = Pt(10)
    doc.add_paragraph()

# ── 3. Configuración del Directorio de Capturas Locales ──
dest_dir = r"d:\DAM\Rutas y Proyectos\Proyectos\DAM Proyecto Final\docs\images"
os.makedirs(dest_dir, exist_ok=True)

def agregar_captura(nombre_archivo, pie_foto):
    ruta_img = os.path.join(dest_dir, nombre_archivo)
    if os.path.exists(ruta_img):
        # Si la captura física existe, la inserta con las dimensiones correctas de Word
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(8)
        p_img.paragraph_format.space_after = Pt(4)
        p_img.paragraph_format.keep_with_next = True
        
        run = p_img.add_run()
        # El logo de portada se añade un poco más ancho que las capturas verticales de móvil
        if nombre_archivo == "logo.png":
            run.add_picture(ruta_img, width=Inches(2.5))
        else:
            run.add_picture(ruta_img, width=Inches(2.8))
        
        cap = doc.add_paragraph()
        cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        cap.paragraph_format.space_after = Pt(8)
        run_cap = cap.add_run(f"Figura: {pie_foto}")
        run_cap.italic = True
        run_cap.font.name = 'Calibri'
        run_cap.font.size = Pt(9.5)
        run_cap.font.color.rgb = RGBColor(0x55, 0x55, 0x55)
    else:
        # Si la captura no existe, genera una caja de sombreado gris académica explicando
        # al usuario exactamente qué archivo guardar en la carpeta docs/images
        p_place = doc.add_paragraph()
        p_place.paragraph_format.left_indent = Cm(1.0)
        p_place.paragraph_format.right_indent = Cm(1.0)
        p_place.paragraph_format.space_before = Pt(8)
        p_place.paragraph_format.space_after = Pt(8)
        
        # Sombreado gris suave
        tcPr = p_place._p.get_or_add_pPr()
        shd = OxmlElement('w:shd')
        shd.set(qn('w:fill'), 'F5F5F5')
        shd.set(qn('w:val'), 'clear')
        tcPr.append(shd)
        
        run = p_place.add_run(
            f"⚠️ [INDICACIÓN DE MAQUETADO DE TFG]\n"
            f"Coloca aquí tu propia captura de pantalla de la: {pie_foto}.\n"
            f"Instrucciones: Toma la captura desde tu dispositivo/emulador, nombra el archivo exactamente "
            f"como '{nombre_archivo}' y guárdalo en la carpeta 'docs/images/'.\n"
            f"Luego, vuelve a ejecutar el script 'generar_memoria.py' para incrustarla automáticamente."
        )
        run.bold = True
        run.font.name = 'Calibri'
        run.font.size = Pt(9.5)
        run.font.color.rgb = RGBColor(0xD3, 0x2F, 0x2F) # Rojo institucional de advertencia
        doc.add_paragraph()

# ══════════════════════════════════════════════════════════════════════════════
# PORTADA
# ══════════════════════════════════════════════════════════════════════════════
for _ in range(3): doc.add_paragraph()

titulo = doc.add_paragraph()
titulo.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = titulo.add_run("MEMORIA TÉCNICA Y DE DISEÑO DE SOFTWARE")
r.bold = True
r.font.name = 'Arial'
r.font.size = Pt(22)
r.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D)

subtitulo = doc.add_paragraph()
subtitulo.alignment = WD_ALIGN_PARAGRAPH.CENTER
r2 = subtitulo.add_run("StudyFiles — Gestor Académico Colaborativo y Nube de Recursos Multidispositivo")
r2.bold = True
r2.font.name = 'Arial'
r2.font.size = Pt(14)
r2.font.color.rgb = RGBColor(0x55, 0x55, 0x55)

for _ in range(2): doc.add_paragraph()

# Añadir el logo real de la app en la portada si existe
agregar_captura("logo.png", "Isotipo oficial de la aplicación StudyFiles")

for _ in range(2): doc.add_paragraph()

datos = doc.add_paragraph()
datos.alignment = WD_ALIGN_PARAGRAPH.CENTER
datos.paragraph_format.line_spacing = 1.3
r_datos = datos.add_run(
    "Autor: Antonio MBA Nzang\n"
    "Ciclo: Desarrollo de Aplicaciones Multiplataforma (DAM) — Grado Superior\n"
    "Centro Docente: EPSUM\n"
    "Tutor del Proyecto: Mario Castro\n"
    "Curso Académico: 2025 / 2026\n"
    "Fecha de Entrega: Mayo de 2026\n"
)
r_datos.font.name = 'Arial'
r_datos.font.size = Pt(11)

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# ÍNDICE DEL PROYECTO (DE CARA AL TFG)
# ══════════════════════════════════════════════════════════════════════════════
h1("ÍNDICE GENERAL DEL PROYECTO (PROPUESTA TFG)")
p("A continuación, se detalla la propuesta formal de estructura e índice general para la defensa del Trabajo de Fin de Grado (TFG) del ciclo formativo de grado superior en DAM:", italic=True)

# Lista estructurada del índice
def item_indice(numero, titulo_seccion, indent=0.0, bold=False):
    p_ind = doc.add_paragraph()
    p_ind.paragraph_format.left_indent = Cm(indent)
    p_ind.paragraph_format.space_after = Pt(2)
    run = p_ind.add_run(f"{numero} {titulo_seccion}")
    run.font.name = 'Calibri'
    run.font.size = Pt(11)
    run.bold = bold

item_indice("1.", "INTRODUCCIÓN Y JUSTIFICACIÓN DEL PROYECTO", bold=True)
item_indice("1.1", "Contexto del problema y estado del arte", indent=0.5)
item_indice("1.2", "La pérdida de recursos académicos interanual", indent=0.5)
item_indice("1.3", "Justificación del desarrollo móvil e híbrido", indent=0.5)

item_indice("2.", "OBJETIVOS Y ALCANCE", bold=True)
item_indice("2.1", "Objetivo principal de la aplicación", indent=0.5)
item_indice("2.2", "Objetivos específicos y técnicos", indent=0.5)
item_indice("2.3", "Alcance funcional del sistema colaborativo", indent=0.5)

item_indice("3.", "ANÁLISIS DE REQUISITOS Y CASOS DE USO", bold=True)
item_indice("3.1", "Requisitos funcionales del sistema", indent=0.5)
item_indice("3.2", "Requisitos no funcionales (seguridad, rendimiento)", indent=0.5)
item_indice("3.3", "Diagramas y modelado de Casos de Uso (CU)", indent=0.5)

item_indice("4.", "ARQUITECTURA DE SOFTWARE Y TECNOLOGÍAS", bold=True)
item_indice("4.1", "La pila tecnológica elegida (Android SDK, Java, Retrofit)", indent=0.5)
item_indice("4.2", "Modelo de capas: Vista, Adaptador, Red, Cache local", indent=0.5)
item_indice("4.3", "Patrones de diseño de software (Singleton, Listener)", indent=0.5)

item_indice("5.", "PERSISTENCIA DE DATOS HÍBRIDA (NUBE Y LOCAL)", bold=True)
item_indice("5.1", "El motor local Room (SQLite): Entidades y DAOs", indent=0.5)
item_indice("5.2", "El motor remoto Supabase (PostgreSQL y REST)", indent=0.5)
item_indice("5.3", "Sincronización híbrida de la nube a través de IDs", indent=0.5)

item_indice("6.", "ANÁLISIS E IMPLEMENTACIÓN DEL CÓDIGO FUENTE", bold=True)
item_indice("6.1", "Estructura del código en paquetes Java", indent=0.5)
item_indice("6.2", "La Actividad Principal y el gestor de tabs", indent=0.5)
item_indice("6.3", "Detalle colaborativo, votos y comentarios", indent=0.5)
item_indice("6.4", "El Tutor de Estudio y la resolución de dudas", indent=0.5)
item_indice("6.5", "El sistema de Nube Personal de archivos", indent=0.5)

item_indice("7.", "DOCUMENTACIÓN DE LAS APIS CONSUMIDAS", bold=True)
item_indice("7.1", "Endpoint de Supabase (Base de datos y Storage)", indent=0.5)
item_indice("7.2", "Consumo asíncrono y mapeado seguro con Retrofit", indent=0.5)

item_indice("8.", "DESARROLLO DE COMPETENCIAS Y RESOLUCIÓN DE PROBLEMAS", bold=True)
item_indice("8.1", "Competencias técnicas del ciclo DAM aplicadas", indent=0.5)
item_indice("8.2", "Resolución de errores: FK en comentarios, Java 8 compatibility", indent=0.5)

item_indice("9.", "CONCLUSIONES Y LÍNEAS DE TRABAJO FUTURO", bold=True)
item_indice("9.1", "Conclusiones del desarrollo del TFG", indent=0.5)
item_indice("9.2", "Futuras ampliaciones del sistema StudyFiles", indent=0.5)

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 1. INTRODUCCIÓN Y JUSTIFICACIÓN
# ══════════════════════════════════════════════════════════════════════════════
h1("1. Introducción y Justificación")
p("El desarrollo de aplicaciones multiplataforma y móviles se ha consolidado como un pilar fundamental en la educación moderna. En el entorno de la Formación Profesional (FP) y la enseñanza reglada superior, se presenta una problemática recurrente: la tremenda dispersión y la posterior pérdida de recursos académicos de gran valor que se generan a lo largo de cada curso escolar.")
p("Al finalizar el año académico, los apuntes, resúmenes elaborados, colecciones de ejercicios resueltos y guías de estudio preparados por los alumnos con mayor rendimiento suelen quedar almacenados de forma local en los dispositivos personales, o se borran de los grupos temporales de mensajería instantánea. Como resultado, los alumnos de las siguientes promociones inician el curso con una absoluta carencia de recursos de apoyo previos, debiendo repetir el proceso de búsqueda y recopilación desde cero.")
p("StudyFiles nace como solución directa a esta carencia, diseñándose como un repositorio y gestor académico colaborativo integrado en una aplicación móvil Android de alto rendimiento. Esta plataforma centraliza el almacenamiento de archivos de estudio en la nube y los organiza de manera intuitiva según asignaturas académicas y categorías. Además de permitir la subida de documentos y la interacción comunitaria a través de comentarios y votos de utilidad, el sistema incorpora una innovadora nube personal de archivos ('Mi Nube') y un asistente virtual de estudio interactivo integrado que guía al usuario en la resolución de dudas y la síntesis de conceptos.")

# ══════════════════════════════════════════════════════════════════════════════
# 2. OBJETIVO PRINCIPAL
# ══════════════════════════════════════════════════════════════════════════════
h1("2. Objetivo Principal del Proyecto")
p("El objetivo principal de StudyFiles es diseñar, desarrollar e implementar de forma integral una aplicación móvil nativa bajo la plataforma Android que sirva como plataforma de almacenamiento, búsqueda y compartición colaborativa de material escolar entre estudiantes.")
p("El sistema persigue democratizar el acceso a los recursos de estudio facilitando una red transparente donde cualquier alumno pueda:")
doc.add_paragraph("Registrarse e iniciar sesión de forma segura para tener una identidad académica propia.", style='List Bullet')
doc.add_paragraph("Explorar y buscar en tiempo real material subido por otros compañeros categorizado de forma didáctica.", style='List Bullet')
doc.add_paragraph("Gestionar un espacio personal privado de archivos en la nube, simulando un disco virtual adaptado.", style='List Bullet')
doc.add_paragraph("Interactuar activamente en la comunidad mediante un completo sistema de comentarios y votos que premie el contenido de alta calidad y lo mantenga libre de spam.", style='List Bullet')
doc.add_paragraph("Consultar a un asistente de estudio virtual interactivo para aclarar conceptos, estructurar temas y resolver cuestionarios de repaso.", style='List Bullet')

# ══════════════════════════════════════════════════════════════════════════════
# 3. ANÁLISIS DE REQUISITOS Y CASOS DE USO
# ══════════════════════════════════════════════════════════════════════════════
h1("3. Análisis de Requisitos y Casos de Uso")
p("Para garantizar el correcto modelado del software y su modularidad, se ha procedido al análisis de los requisitos funcionales del sistema, los cuales se describen a través de los siguientes Casos de Uso principales:")

tabla(["Caso de Uso", "Actor", "Descripción del Flujo", "Resultado Esperado"], [
    ["CU-01: Autenticación", "Usuario", "El usuario rellena correo y contraseña para hacer login o registro remoto.", "Token JWT guardado y acceso a la app."],
    ["CU-02: Búsqueda", "Estudiante", "Introduce palabras clave en la barra superior del buscador integrado.", "Filtrado interactivo en tiempo real."],
    ["CU-03: Subida", "Estudiante", "Completa el formulario de detalles de archivo y selecciona el documento.", "Archivo subido a la nube y enlazado."],
    ["CU-04: Comentar", "Estudiante Reg.", "Añade aportaciones, sugerencias o dudas al hilo de un archivo.", "Persistencia y renderizado inmediato."],
    ["CU-05: Nube Personal", "Estudiante", "Crea carpetas virtuales y sube archivos de forma privada a la nube.", "Estructura jerárquica guardada."],
    ["CU-06: Consulta Tutor", "Estudiante", "Envía una pregunta en lenguaje natural al tutor en la vista del chat.", "Respuesta inteligente basada en apuntes."]
])

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 4. TECNOLOGÍAS Y HERRAMIENTAS
# ══════════════════════════════════════════════════════════════════════════════
h1("4. Tecnologías y Herramientas")
p("Para asegurar la máxima compatibilidad, estabilidad y el cumplimiento de las directrices académicas del grado superior en DAM, se estructuró una pila de tecnologías robusta y compatible con la inmensa mayoría de dispositivos móviles actuales:")

h2("4.1 Plataforma de Desarrollo y Entorno")
doc.add_paragraph("Lenguaje Java (JDK 8/17): Se ha optado por Java debido a su tipado estático fuerte y su absoluta cohesión con los paradigmas del desarrollo de interfaces en Android de forma nativa.", style='List Bullet')
doc.add_paragraph("Android SDK 34 (Android 14): Permite el aprovechamiento de APIs modernas de gestión y permisos, garantizando retrocompatibilidad hasta la API 26 (Android 8.0).", style='List Bullet')
doc.add_paragraph("Android Studio (Hedgehog): IDE utilizado para el maquetado, escritura y depuración del proyecto.", style='List Bullet')

h2("4.2 Persistencia Local de Datos — Room ORM")
p("Para la caché y la base de datos local embebida en el dispositivo, se implementó el framework Room, el cual actúa sobre SQLite abstrayendo al desarrollador del mapeo manual de las consultas relacionales mediante clases Java (entidades) y DAOs (Data Access Objects). La base de datos local gestiona de forma interactiva el historial de favoritos del usuario y el control del estado local de los votos para evitar duplicidades.")

h2("4.3 Conectividad y Red — Retrofit 2 y OkHttp 3")
p("La comunicación entre la aplicación cliente y la nube (Supabase REST API y asistentes virtuales) se centraliza a través de un cliente HTTP altamente optimizado: Retrofit 2. Este cliente permite abstraer las cabeceras HTTP, la conversión automática de JSON a modelos Java mediante Gson y la ejecución segura de hilos asíncronos mediante callbacks en cola de fondo.")

# ══════════════════════════════════════════════════════════════════════════════
# 5. ARQUITECTURA DEL SISTEMA
# ══════════════════════════════════════════════════════════════════════════════
h1("5. Arquitectura del Sistema")
p("El sistema está diseñado bajo el patrón clásico por capas en Android, lo que garantiza el aislamiento de la lógica de negocio respecto a la renderización gráfica de la vista y la procedencia de los datos. Esta separación facilita la mantenibilidad del código ante cambios futuros en las APIs externas.")

p("Las capas del proyecto se organizan del siguiente modo:")
doc.add_paragraph("Capa de Interfaz y Presentación (Activities y Adaptadores): Compuesta por las clases encargadas del dibujado de la pantalla, la vinculación de vistas con findViewById() y la captura del click del usuario. Los adaptadores son esenciales en esta capa, pues gestionan la inyección eficiente de datos dinámicos en los RecyclerView.", style='List Bullet')
doc.add_paragraph("Capa de Modelos de Datos: Clases Java estándar (POJOs) que representan las entidades del dominio (ej. Archivo, Comentario, MiCarpeta). Estas clases incorporan las anotaciones de SerializedName de Gson para mapear de manera transparente las respuestas JSON de Supabase.", style='List Bullet')
doc.add_paragraph("Capa de Persistencia y Caché Local (Room Database): Define el acceso físico a la base de datos local SQLite instalada en el almacenamiento del dispositivo. Es utilizada principalmente para la persistencia offline del módulo de favoritos.", style='List Bullet')
doc.add_paragraph("Capa de Cliente y API de Red (Retrofit y SupabaseClient): Gestiona la creación del cliente HTTP asíncrono y los interceptores necesarios para inyectar cabeceras de autorización JWT en cada petición remota.", style='List Bullet')

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 6. BASE DE DATOS Y MODELO DE DATOS
# ══════════════════════════════════════════════════════════════════════════════
h1("6. Base de Datos y Modelo de Datos")
p("Para el almacenamiento y la coherencia estructural de la información de StudyFiles, se utiliza un sistema híbrido:")

h2("6.1 Estructura Local Embebida (Room Database)")
p("Se implementa la base de datos local encapsulada en la clase BaseDatos, la cual incrementa a la versión 3 de esquema y se configura sin exportar esquemas locales. Dicha clase expone dos métodos abstractos de acceso a DAOs:")
codigo_bloque(
    "@Database(\n"
    "    entities = { Favorito.class, VotoLocal.class },\n"
    "    version = 3,\n"
    "    exportSchema = false\n"
    ")\n"
    "public abstract class BaseDatos extends RoomDatabase {\n"
    "    private static BaseDatos INSTANCE;\n"
    "    public abstract FavoritoDao favoritoDao();\n"
    "    public abstract VotoLocalDao votoLocalDao();\n"
    "    ...\n"
    "}"
)
p("La entidad Favorito mapea la información clave de los apuntes guardados por el usuario para su rápida consulta e incluso su lectura sin conectividad a internet. Por otro lado, la tabla VotoLocal realiza un seguimiento de los archivos que ya han recibido un voto positivo o negativo por parte del dispositivo, evitando dobles clics inválidos.")

h2("6.2 Estructura Remota en la Nube (Supabase / Postgres)")
p("El almacenamiento global y colaborativo reside en una base de datos relacional PostgreSQL levantada en Supabase. Las tablas clave de este backend son:")
doc.add_paragraph("public.archivos: Almacena el identificador único numérico del archivo, el nombre, la descripción, la URL final de descarga del fichero (.pdf, .docx, .png) alojado en el Storage, la categoría correspondiente y las estadísticas asociadas.", style='List Bullet')
doc.add_paragraph("public.comentarios: Permite asociar comentarios a las publicaciones. Mapea la clave foránea publicacion_id hacia archivos.id y el usuario_id del estudiante correspondiente en formato string.", style='List Bullet')
doc.add_paragraph("public.carpetas: Permite simular una estructura de carpetas en la nube para 'Mi Nube'. Cada fila guarda el id de la carpeta, el nombre, el id de su carpeta padre (para permitir recursividad jerárquica) y el usuario dueño.", style='List Bullet')

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 7. MÓDULOS E IMPLEMENTACIÓN DEL CÓDIGO FUENTE
# ══════════════════════════════════════════════════════════════════════════════
h1("7. Módulos e Implementación del Código Fuente")
p("En este bloque técnico, se realiza el análisis profundo de la implementación del código fuente del proyecto StudyFiles, desglosando la responsabilidad y la codificación de las principales Activities que estructuran la aplicación.")

h2("7.1 Actividad Principal (ActividadPrincipal.java)")
p("Constituye la pantalla de entrada central de la aplicación. Su rol consiste en gestionar la barra superior de búsqueda interactiva, los RecyclerView para las categorías didácticas y un BottomNavigationView que permite alternar la vista entre el muro principal de inicio, el explorador privado de la nube ('Mi Nube') y los favoritos guardados de forma local.")
agregar_captura("pantalla_principal.png", "Vista principal de StudyFiles con categorías, tabs y el botón de acceso al tutor.")
p("Código clave de inicialización de tabs y adaptadores de categorías en la actividad principal:")
codigo_bloque(
    "private void configurarCategorias() {\n"
    "    List<AdaptadorCategorias.Categoria> cats = Arrays.asList(\n"
    "        new AdaptadorCategorias.Categoria(\"Historia\", R.drawable.ic_historia, R.color.cat_historia),\n"
    "        new AdaptadorCategorias.Categoria(\"Matemáticas\", R.drawable.ic_matematicas, R.color.cat_matematicas),\n"
    "        new AdaptadorCategorias.Categoria(\"Lengua\", R.drawable.ic_lengua, R.color.cat_lengua),\n"
    "        new AdaptadorCategorias.Categoria(\"Ciencias\", R.drawable.ic_ciencias, R.color.cat_ciencias),\n"
    "        new AdaptadorCategorias.Categoria(\"Informática\", R.drawable.ic_informatica, R.color.cat_informatica),\n"
    "        new AdaptadorCategorias.Categoria(\"Inglés\", R.drawable.ic_ingles, R.color.cat_ingles),\n"
    "        new AdaptadorCategorias.Categoria(\"Arte y Música\", R.drawable.ic_arte, R.color.cat_arte),\n"
    "        new AdaptadorCategorias.Categoria(\"Otros\", R.drawable.ic_otros, R.color.cat_otros)\n"
    "    );\n"
    "    AdaptadorCategorias adapter = new AdaptadorCategorias(cats, cat -> {\n"
    "        Intent i = new Intent(this, ActividadArchivos.class);\n"
    "        i.putExtra(\"categoria\", cat.nombre);\n"
    "        startActivity(i);\n"
    "    });\n"
    "    rvCategorias.setLayoutManager(new GridLayoutManager(this, 2));\n"
    "    rvCategorias.setAdapter(adapter);\n"
    "}"
)

doc.add_page_break()

h2("7.2 Actividad de Detalle Académico (ActividadDetalle.java)")
p("Esta es la pantalla de mayor peso lógico del proyecto, responsable del renderizado completo de las publicaciones académicas subidas. Esta clase integra:")
doc.add_paragraph("1. Gestión de Contadores y Votos: Likes y dislikes consumiendo la base de datos local y remota para evitar abusos.", style='List Bullet')
doc.add_paragraph("2. Sistema de Comentarios Colaborativos: Carga asíncrona mediante un AdaptadorComentarios que implementa opciones de eliminación y edición solo para el autor legítimo del comentario.", style='List Bullet')
doc.add_paragraph("3. Lógica de Descargas Seguras: Descarga de ficheros remotos a la carpeta local de Downloads mediante peticiones OkHttp.", style='List Bullet')
agregar_captura("pantalla_detalle.png", "Detalle de un archivo académico con comentarios integrados y votos de utilidad.")

p("Código clave de envío de comentarios en la ActividadDetalle:")
codigo_bloque(
    "private void enviarComentario() {\n"
    "    String texto = etComentario.getText().toString().trim();\n"
    "    if (texto.isEmpty()) return;\n"
    "    etComentario.setText(\"\");\n"
    "    String usuarioId = getIdentificador();\n"
    "    Comentario request = new Comentario((long) archivoId, usuarioId, texto);\n"
    "\n"
    "    SupabaseClient.getApi().crearComentario(request).enqueue(new Callback<List<Comentario>>() {\n"
    "        @Override\n"
    "        public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {\n"
    "            if (response.isSuccessful()) {\n"
    "                cargarComentarios(); // Recarga interactiva\n"
    "            } else {\n"
    "                Toast.makeText(ActividadDetalle.this, \"Error al enviar comentario\", Toast.LENGTH_SHORT).show();\n"
    "            }\n"
    "        }\n"
    "        @Override\n"
    "        public void onFailure(Call<List<Comentario>> call, Throwable t) {\n"
    "            Toast.makeText(ActividadDetalle.this, \"Error de red\", Toast.LENGTH_SHORT).show();\n"
    "        }\n"
    "    });\n"
    "}"
)

doc.add_page_break()

h2("7.3 Tutor Académico Virtual (ActividadChat.java)")
p("El tutor interactivo ofrece asistencia en lenguaje natural basada en el material subido a la aplicación (estrategia de búsqueda y respuesta contextual). El flujo de mensajes utiliza un RecyclerView dinámico y está conectado a una base de datos en Supabase para persistir el historial de mensajes de cada estudiante.")
agregar_captura("pantalla_chat.png", "Interfaz del chat interactivo con el Tutor Virtual de StudyFiles.")

p("Lógica de envío de mensajes e interacción con el helper en la ActividadChat:")
codigo_bloque(
    "private void enviarMensaje() {\n"
    "    String texto = etMensaje.getText().toString().trim();\n"
    "    if (texto.isEmpty()) return;\n"
    "    etMensaje.setText(\"\");\n"
    "    btnEnviar.setEnabled(false);\n"
    "    pbEscribiendo.setVisibility(View.VISIBLE);\n"
    "\n"
    "    // 1. Crear y mostrar mensaje del usuario\n"
    "    MensajeChat msgUser = new MensajeChat(usuarioId, \"user\", texto, System.currentTimeMillis());\n"
    "    adaptador.agregarMensaje(msgUser);\n"
    "    scrollToBottom();\n"
    "\n"
    "    // 2. Guardar en la nube\n"
    "    guardarMensajeEnSupabase(msgUser);\n"
    "\n"
    "    // 3. Consultar asíncronamente con el helper contextual\n"
    "    GeminiChatHelper.enviarMensajeChat(historial, texto, new GeminiChatHelper.GeminiCallback() {\n"
    "        @Override\n"
    "        public void onSuccess(String result) {\n"
    "            btnEnviar.setEnabled(true);\n"
    "            pbEscribiendo.setVisibility(View.GONE);\n"
    "            MensajeChat msgTutor = new MensajeChat(usuarioId, \"model\", result, System.currentTimeMillis());\n"
    "            adaptador.agregarMensaje(msgTutor);\n"
    "            scrollToBottom();\n"
    "            guardarMensajeEnSupabase(msgTutor);\n"
    "        }\n"
    "        @Override\n"
    "        public void onError(String error) {\n"
    "            btnEnviar.setEnabled(true);\n"
    "            pbEscribiendo.setVisibility(View.GONE);\n"
    "            Toast.makeText(ActividadChat.this, \"Error del Tutor: \" + error, Toast.LENGTH_LONG).show();\n"
    "        }\n"
    "    });\n"
    "}"
)

doc.add_page_break()

h2("7.4 Módulo de Nube Personal (ActividadCarpeta.java)")
p("Este módulo otorga al estudiante un espacio jerárquico privado en la nube de Supabase. A través de la ActividadCarpeta, el usuario puede explorar subcarpetas de manera recursiva, crear nuevos directorios virtuales y subir ficheros asociados directamente a una carpeta específica.")
agregar_captura("pantalla_carpeta.png", "Gestor de carpetas y archivos privados del estudiante en 'Mi Nube'.")

h2("7.5 Formulario de Subida de Ficheros (ActividadSubir.java)")
p("Gestiona la subida de material de apuntes públicos a la red. El usuario completa los datos básicos (nombre, descripción, categoría, autor) y de forma opcional campos de metadatos de calidad (institución académica y nivel de estudios), seleccionando el fichero del almacenamiento local de su dispositivo móvil.")
agregar_captura("pantalla_subir.png", "Formulario de publicación de apuntes escolares con detalles académicos.")

h2("7.6 Autenticación y Registro (ActividadLogin.java)")
p("Gestiona la pantalla de entrada a la aplicación. Valida que el email tenga el formato correcto y realiza la petición HTTP POST hacia el servidor remoto de Supabase Auth para verificar el token JWT de la sesión.")
agregar_captura("pantalla_login.png", "Pantalla de inicio de sesión segura y acceso a la plataforma.")

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 8. DOCUMENTACIÓN DE LAS APIS
# ══════════════════════════════════════════════════════════════════════════════
h1("8. Documentación de las APIs")
p("Para interactuar con la nube y el resto de los servicios remotos, StudyFiles expone y consume un conjunto de APIs mediante Retrofit. A continuación, se documentan las llamadas y sus estructuras:")

h2("8.1 Endpoint de la Base de Datos Remota (Supabase REST API)")
p("URL Base: https://flpdwxgobctdkudovdyx.supabase.co/rest/v1/", bold=True)
p("Cabeceras obligatorias requeridas en el interceptor de red:")
doc.add_paragraph("apikey: [Clave pública API de Supabase]", style='List Bullet')
doc.add_paragraph("Authorization: Bearer [Token JWT del usuario obtenido en el login]", style='List Bullet')
doc.add_paragraph("Content-Type: application/json", style='List Bullet')

p("Estructuras y métodos clave en la interfaz Java SupabaseApi:")
codigo_bloque(
    "public interface SupabaseApi {\n"
    "    @GET(\"archivos\")\n"
    "    Call<List<Archivo>> buscarArchivosAvanzado(@Query(\"or\") String orQuery);\n"
    "\n"
    "    @GET(\"archivos\")\n"
    "    Call<List<Archivo>> getArchivoPorId(@Query(\"id\") String idFiltro);\n"
    "\n"
    "    @POST(\"comentarios\")\n"
    "    Call<List<Comentario>> crearComentario(@Body Comentario request);\n"
    "}"
)

h2("8.2 Endpoint de la API del Tutor Virtual (Conectividad Segura)")
p("URL Base: https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=[CLAVE_SEGURA]", bold=True)
p("La llamada se realiza de forma directa por POST utilizando OkHttp 3. El cuerpo de la petición sigue la estructura jerárquica de turnos (User/Model) requerida por el backend del proveedor generativo:")
codigo_bloque(
    "{\n"
    "  \"contents\": [\n"
    "    { \"role\": \"user\", \"parts\": [{ \"text\": \"[System Prompt + Contexto]\" }] },\n"
    "    { \"role\": \"model\", \"parts\": [{ \"text\": \"Entendido. Asistiré al alumno.\" }] },\n"
    "    { \"role\": \"user\", \"parts\": [{ \"text\": \"¿Qué es la programación orientada a objetos?\" }] }\n"
    "  ],\n"
    "  \"generationConfig\": {\n"
    "    \"temperature\": 0.7,\n"
    "    \"maxOutputTokens\": 1024\n"
    "  }\n"
    "}"
)

doc.add_page_break()

# ══════════════════════════════════════════════════════════════════════════════
# 9. DESARROLLO DE COMPETENCIAS Y RESOLUCIÓN DE PROBLEMAS
# ══════════════════════════════════════════════════════════════════════════════
h1("9. Desarrollo de Competencias y Resolución de Problemas")
p("Durante el ciclo de desarrollo del proyecto StudyFiles, se han aplicado y afianzado múltiples competencias técnicas contempladas en el currículo oficial del grado superior en Desarrollo de Aplicaciones Multiplataforma (DAM):")

h2("9.1 Competencias Técnicas Específicas Aplicadas")
doc.add_paragraph("Programación Multimedia y Dispositivos Móviles: Creación de hilos secundarios seguros, ciclo de vida de las Activities Android, paso de parámetros estructurados mediante bundles en los Intents e inflado y renderizado dinámico de interfaces responsivas.", style='List Bullet')
doc.add_paragraph("Acceso a Datos y Persistencia: Manejo avanzado de ORMs mediante Room, definición de relaciones relacionales locales (1:N), abstracción del lenguaje SQL directo y diseño de sincronización híbrida local/nube.", style='List Bullet')
doc.add_paragraph("Desarrollo de Interfaces: Maquetado mediante ficheros XML empleando Material Design 3, paletas de colores dinámicas compatibles con modo claro y modo nocturno, y adaptabilidad a distintos tamaños de pantalla.", style='List Bullet')
doc.add_paragraph("Servicios y Procesos: Consumo seguro y asíncrono de APIs RESTful usando Retrofit 2 en cola de fondo de forma compatible con la UI principal de Android.", style='List Bullet')

h2("9.2 Resolución de Problemas Técnicos Complejos")
p("A lo largo del proyecto surgieron retos de gran calibre técnico que requirieron de un profundo análisis técnico para ser solucionados de forma elegante y compatible:")
doc.add_paragraph("1. Conflicto de Claves Foráneas (FK) en Base de Datos: Durante el diseño del módulo de comentarios colaborativos, se detectó una violación de clave foránea al intentar guardar registros en Supabase. El problema residía en que la tabla remota comentarios apuntaba incorrectamente a una tabla publicaciones inexistente. Se solucionó reconfigurando la relación en Supabase para asociar directamente la columna publicacion_id con el id de archivos. Además, en el código Java se formateó de manera estricta el usuarioId agregando el prefijo oficial 'user_' y se casteó la ID del archivo a tipo Long.", style='List Bullet')
doc.add_paragraph("2. Compatibilidad del Lector de Archivos con Java 8: En la subida de ficheros privados a la nube, inicialmente se empleó el método Files.readAllBytes(), el cual provocaba fallas de compilación al estar obsoleto en la versión de Java del compilador de Android del proyecto. Se reescribió la lógica utilizando flujos clásicos (InputStream y ByteArrayOutputStream) totalmente compatibles con Java 8, asegurando el correcto procesado de ficheros independientemente del dispositivo de ejecución.", style='List Bullet')

# ══════════════════════════════════════════════════════════════════════════════
# 10. CONCLUSIONES Y TRABAJO FUTURO
# ══════════════════════════════════════════════════════════════════════════════
h1("10. Conclusiones y Trabajo Futuro")

h2("10.1 Conclusiones")
p("El desarrollo del proyecto final de grado superior StudyFiles ha constituido una experiencia de aprendizaje integradora de un valor inestimable. Ha permitido condensar todos los conocimientos teóricos adquiridos a lo largo de los dos cursos de DAM y aplicarlos de forma práctica en un producto de software real de extremo a extremo.")
p("El resultado es un sistema móvil estable, rápido y dotado de una experiencia de usuario sobresaliente y moderna, que proporciona una utilidad real a la comunidad educativa.")

h2("10.2 Trabajo Futuro")
p("A pesar del excelente resultado obtenido en esta versión 1.0, el software se ha diseñado de forma modular para permitir una fácil escalabilidad y la inyección de nuevas características a futuro:")
doc.add_paragraph("Sistema de Notificaciones Push Remotas: Para avisar instantáneamente al autor de un archivo cuando otro estudiante añada un comentario o vote positivamente su aporte.", style='List Bullet')
doc.add_paragraph("Mensajería Privada entre Estudiantes: Crear salas de chat directas cifradas de extremo a extremo para coordinar trabajos en grupo.", style='List Bullet')
doc.add_paragraph("Migración del Motor de Interfaz a Jetpack Compose: Para dar el salto a un desarrollo declarativo moderno y reducir el código de renderización XML de la vista.", style='List Bullet')

doc.add_paragraph()
final = doc.add_paragraph()
final.alignment = WD_ALIGN_PARAGRAPH.CENTER
final.paragraph_format.space_before = Pt(20)
r_fin = final.add_run("Fin de la Memoria Técnica de Ingeniería de Software — StudyFiles v1.0\n"
                      "Antonio MBA Nzang — Ciclo Superior DAM — EPSUM 2026")
r_fin.italic = True
r_fin.font.size = Pt(10.0)

# ── 4. Guardar Documento de forma robusta ──
ruta_final = r"d:\DAM\Rutas y Proyectos\Proyectos\DAM Proyecto Final\MEMORIA_TECNICA_STUDYBRO.docx"
try:
    doc.save(ruta_final)
    print(f"OK - Documento guardado en: {ruta_final}")
except PermissionError:
    # Si el archivo está abierto en MS Word, lo guardamos con un sufijo temporal para no perder la generación
    ruta_alt = r"d:\DAM\Rutas y Proyectos\Proyectos\DAM Proyecto Final\MEMORIA_TECNICA_STUDYBRO_COMPILADA.docx"
    doc.save(ruta_alt)
    print(f"ATENCION - Archivo principal bloqueado (¿abierto en Word?). Guardado alternativamente en: {ruta_alt}")
