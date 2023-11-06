# MVVM Feature Layered Template

## Introducción

Este proyecto está pensado para comportarse como una plantilla genérica basada en el
patrón MVVM, teniendo todo su código organizado según el esquema de arquitectura Feature-Layered,
el cual permite definir cada feature de la app a desarrollar como módulos completamente separados,
siguiendo cada feature su propio patrón MVVM. De este modo conseguimos desacoplar de forma completa
cada feature, evitando accesos indeseados entre features, o incluso entre casos de uso que no deban
ser accesibles.

Este proyecto consta de un módulo app, que será el responsable de la ejecución de la app, a partir
de la navegación que definamos, siendo el módulo app el único que verá todas las capas de
presentación de cada feature (**ojo, el módulo app sólo tendrá acceso a las capas de UI de cada
feature, ni siquiera tendrá acceso a los ViewModels**).

Hemos comentado que este proyecto está organizado en base al esquema de arquitectura Feature-Layered,
pero crear un módulo MVVM completo con sus capas para cada feature, en caso de hacerlo a mano, puede
ser una tarea extremadamente compleja, por ello, he desarrollado una tarea personalizada para Gradle
que, con un sencillo comando, nos va a crear toda la estructura de la feature (con los módulos y
submódulos necesarios), dejándonosla preparada para comenzar la implementación de cada funcionalidad.

### Ventajas e inconvenientes de un esquema de arquitectura Feature-Layered

Como todo, este esquema de desarrollo tiene una serie de ventajas e inconvenientes que deberemos
tener en cuenta a la hora de decidirnos por su implementación:

#### Ventajas

- Conseguimos una segmentación completa del proyecto, acotando las responsabilidades de cada proceso
  definido de forma clara.
- Cada feature podría tener sus propias características y dependencias.
- Permite el trabajo en paralelo de distintos equipos de trabajo (un equipo de desarrollo para cada
  feature, por ejemplo).
- Si tenemos n módulos, de los cuales sólo hemos tocado uno y tenemos una compilación previa hecha,
  tan solo se compilará el módulo app y la feature modificada (la capa modificada de dicha feature),
  gracias a la compilación incremental.

#### Inconvenientes

- Se incrementa la complejidad de los ficheros *.gradle.kts, ya que hay tantos build.gradle.kts como
  módulos tengamos en nuestro proyecto.
- A raíz del punto anterior, se incrementan los tiempos de compilación, ya que el entorno debe
  gestionar más ficheros de configuración, y gestionar las compilaciones de cada proceso, este punto
  se verá minimiado por la cuarta ventaja, ya que con las compilaciones incrementales el "daño" se
  reduce.

## Arquitectura

Esta plantilla dispone de un módulo principal app, que es el encargado de albergar el manifest y la
actividad encargada de la ejecución de nuestra app. Además este módulo app tendrá una referencia a
cada capa de presentación de cada feature que implementemos, teniendo de este modo garantizado que
dicha capa app solo va a tener acceso a la capa de presentación de cada feature, y no a, por ejemplo,
los viewmodels, casos de uso, etc.

Así mismo, cada feature está estructurada siguiendo un patrón MVVM, el cual, organiza una serie de
módulos de librería android en las clásicas capas (presentatio, domain data). Veamos a continuación
cómo se estructura cada capa:

- **presentation**, define la capa de interacción con el usuario. Alberga dós módulos:
    - *ui*, en este módulo definiremos todas las interfaces de usuario que requiera la feature, ya sean
      composables o vistas clásicas, tiene como dependencia directa a la capa de viewmodels y los modelos
      de dominio.
    - *viewmodels*, en este módulo albergamos los viewmodels que nuestra feature requiera. Tiene como
      únicas dependencias la capa de casos de uso de domain y los modelos de dominio.
- **domain**, define la capa de dominio, en la que definiremos la lógica de negocio, casos de uso,
  interfaces de repositorios, modelos de datos, etc. Alberga los siguientes módulos:
    - *usecase*, en este módulo implementaremos *todos los casos de uso* que requiramos en nuestra
      feature. Como dependencias, tiene a repository y models (interfaces de repositorio y modelos de
      dominio).
    - *repository*, este módulo define las distintas interfaces de repositorio que la feature requiera.
      Este módulo tiene como única dependencia a models, esto es, los modelos de dominio.
    - *models*, se definen los modelos de dominio, que serán utilizados tanto por el resto de módulos de
      domain, presentation y las implementaciones de repositorios en data.
- **data**, se define la capa de acceso a datos. En esta capa se deben definir **todos los posibles
  orígenes de datos que puedan proporcionar información a la feature**, por ejemplo, esta capa debe ser
  la responsable de la obtención de datos de bases de datos, servicios rest, dispositivos bluetooth,
  gps, etc. Alberga los siguientes módulos (esta capa es la más compleja con diferencia):
    - *repository*, en esta capa implementaremos los distintos repositorios definidos en la capa de dominio,
      utilizando para ello las referencias obtenidas a partir de la capa gateway, por tanto, esta capa
      tendrá acceso a:
        - domain->repository
        - domain->models
        - data->models
        - data->gateway
    - *gateway*, en esta capa (similar en funcionalidad a domain->repository), definimos las interfaces
      que nos van a permitir la obtención de datos de los distintos orígenes de datos que soporte nuestra
      app.
    - *datasources*, se implementan las interfaces definidas en gateway, obteniendo datos de cualquier
      origen de datos posible (base de datos local, servicios rest, bluetooth, gps, etc).
    - *models*, en esta capa definimos los distintos modelos de datos de la feature, organizados en base
      a su posible origen / destino (local, remote, bluetooth, etc).

Esquemáticamente la arquitectura de la app (con la definición de features) sería la siguiente:

![MVVM Feature Layered Arch.png](art%2FMVVM%20Feature%20Layered%20Arch.png)

## Definición y creación de Features

Esta plantilla tiene como principal particularidad que está organizada en base a que cada feature
es totalmente independiente de otra, teniendo cada feature su propio patrón de arquitectura MVVM,
en principio, si para cada feature debemos definir esta cantidad de módulos de forma manual tendríamos
un serio problema, ya que no es un patrón sencillo de desplegar.

Para subsanar el problema descrito en el párrafo anterior, se ha implementado en esta
plantilla una tarea gradle que, a partir de una configuración de plantilla básica, permite
mediante la ejecución en consola de dicha tarea y un par de parámetros crear nuestra nueva
feature en tan solo unos segundos.

La tarea para creación de nuevas features se llama **createFeature**, la cual expone a gradle un
par de parámetros:
- **featureName**, mediante este parámetro indicamos el nombre de la nueva feature, lo más común será
  nombrar cada nueva feature siguiendo el patrón **feature:<nombreFeature>**, donde la parte variable
  será <nombreFeature>. Por ejemplo, para una feature de splash, utilizaríamos **feature:splash**.
- **featurePackage**, con este parámetro definimos el package base para la feature, es decir, cada
  módulo de la feature tendrá su propio package, pero todas compartirán esta base como parte común.

### Creación de una nueva feature

Para crear una nueva feature, tan sólo deberemos abrir una consola desde el entorno de desarrollo
(Android Studio) y ejecutar el siguiente comando (para, en el caso del ejemplo, crear la feature
splash):

```bash
./gradlew createFeature --featureName="feature:splash" --featurePackage="io.github.afalabarce.mvvmfeaturedlayered.feature.splash"
```

Una vez finalizado el proceso, tan solo deberemos sincronizar nuestro proyecto con la nueva
configuración de gradle, teniendo ya disponible nuestra nueva feature, enlazada como dependencia
en el módulo app del proyecto.

## Configuraciones, dependencias y tecnologías implementadas

Este proyecto dispone de una serie de dependencias y tecnologías configuradas por defecto que
permitirán el inicio de un proyecto de una forma bastante rápida, ya que cada capa define sólo
lo que necesita, evitando redundancias o dependencias innecesarias. Así pues, visto por capas,
disponemos de las siguientes tecnologías / dependencias implementadas:

- **version catalog**, para gestión eficiente de dependencias.
- **Dagger-Hilt**, como sistema de inyección de dependencias.
- **Test Unitarios con jUnit5**, se implementa el desarrollo de [Marcel Schnelle](https://github.com/mannodermaus/android-junit5),
  así como las funciones de extensión e infix de [Kluent](https://markusamshove.github.io/Kluent/).
- **Corrutinas**, todo el proyecto está implementando pensando en la utilización de corrutinas a
  todos los niveles.
- **Flow**, ya que utilizamos corutinas, ¿por qué no utilizar flows, que son el nuevo estandar?
  Esto implica que no se han agregado dependencias para [LiveData](https://developer.android.com/topic/libraries/architecture/livedata).
- **Jetpack Datastore**, es el nuevo sistema de almacenamiento reactivo de preferencias en apps
  android, en detrimento de las obsoletas y síncronas SharedPreferences.
- **Room**, es el ORM de facto para android, por lo que no hay mucho más que agregar.
- **Retrofit**, para Android, es el estandar de facto para acceso a apis REST.
- **Jetpack Compose**, en las capas de UI, tanto de cada feature como el módulo app, se ha preparado
  todo para poder utilizar el nuevo sistema declarativo de vistas (en su versión para material3),
  aunque se da soporte a vistas clásicas.
- **Coil**, es un conjunto de elementos que permite la visualización reactiva y asíncrona de imágenes,
  totalmente adaptado a Compose.
- **Paging3**, api que facilita los procesos de paginado en apis. (Desarrollo muy rápido de paginaciones
  infinitas con Compose, por ejemplo).
- **JetpackCompose Navigation**, se agregan las dependencias para ejecutar correctamente la navegación
  entre pantallas desarrolladas con Jetpack Compose.
- **Autenticación Biométrica**, con estas dependencias podremos gestionar los procesos de autenticación
  biométrica utilizando el subsistema biométrico de Android.
- **Diversas librerías y dependencias accompanist**, las librerías accompanist son librerías en estado
  alfa que google prueba para a futuro integrar en las apis estándar.
    - *Gestión de permisos*, una forma muchísimo más cómoda de gestionar permisos de usuario.
      Totalmente integrado con Jetpack Compose.
    - **SwipeToRefresh**, permite el clásico pull to refresh con compose.

## Consejos

Ya que estas plantillas están vacías y no tienen nada de código, como sugerencia, se podría utilizar
parte de los desarrollos de mi [Plantilla MVVM](https://github.com/afalabarce/MVVMProject-Hilt), en
la cual hay elementos inicializados para la creación de conexiones a apis rest, preparación de bases
de datos ROOM, preparadas para la creación de migraciones, etc.

## Conclusiones

Esta plantilla es un ejercicio de diseño de un proyecto complejo, en vistas a la creación de un
proyecto altamente escalable y modular, en el que podrían participar distintos equipos de
desarrollo de una escala indeterminada.

Espero que, al menos, pueda servir como elemento didáctivo, con el que practicar, y/o mejorar
las distintas skills de aquel desarrollador que considere su estudio o utilización.




