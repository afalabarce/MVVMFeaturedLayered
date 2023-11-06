# MVVM Feature Layered Template
## Introduction
This project is intended to behave as a generic template based on the MVVM pattern, with all its code organized according to the Feature-Layered architecture scheme. This scheme allows defining each app feature as completely separate modules, each following its own MVVM pattern. This way, we achieve complete decoupling of each feature, avoiding unwanted access between features or even between use cases that should not be accessible.
This project consists of an app module, which will be responsible for executing the app based on the defined navigation. The app module is the only one that will see all the presentation layers of each feature (**note that the app module only has access to the UI layers of each feature, it doesn't even have access to the ViewModels**).
We have mentioned that this project is organized based on the Feature-Layered architecture scheme, but manually creating a complete MVVM module with its layers for each feature can be an extremely complex task. Therefore, I have developed a custom Gradle task that, with a simple command, will create the entire feature structure (with the necessary modules and submodules), leaving it ready for the implementation of each functionality.
### Advantages and Disadvantages of Feature-Layered Architecture
Like everything else, this development scheme has a series of advantages and disadvantages that should be taken into account when deciding on its implementation:
#### Advantages
- We achieve complete segmentation of the project, clearly defining the responsibilities of each defined process.
- Each feature can have its own characteristics and dependencies.
- It allows parallel work of different teams (one development team for each feature, for example).
- If we have n modules, of which we have only touched one and have a previous compilation done, only the app module and the modified feature (the modified layer of that feature) will be compiled, thanks to incremental compilation.
#### Disadvantages
- The complexity of the *.gradle.kts files increases, as there will be as many build.gradle.kts files as modules in our project.
- Due to the previous point, compilation times increase, as the environment must manage more configuration files and handle the compilations of each process. However, this point is minimized by the fourth advantage, as incremental compilations reduce the "damage".
## Architecture
This template has a main app module, which houses the manifest and the activity responsible for running our app. Additionally, this app module will have a reference to each presentation layer of each feature we implement, ensuring that the app layer only has access to the presentation layer of each feature, and not to, for example, the ViewModels, use cases, etc.
Likewise, each feature is structured following the MVVM pattern, which organizes a series of Android library modules into the classic layers (presentation, domain, data). Let's see how each layer is structured:
- **presentation**: defines the user interaction layer. It consists of two modules:
    - *ui*: in this module, we define all the user interfaces required by the feature, whether they are composables or classic views. It has direct dependencies on the ViewModels layer and the domain models.
    - *viewmodels*: in this module, we house the ViewModels required by our feature. Its only dependencies are the domain use case layer and the domain models.
- **domain**: defines the domain layer, where we define the business logic, use cases, repository interfaces, data models, etc. It consists of the following modules:
    - *usecase*: in this module, we implement all the use cases required by our feature. Its dependencies are the repository layer and the domain models (repository interfaces and domain models).
    - *repository*: this module defines the different repository interfaces required by the feature. It has only one dependency, the domain models.
    - *models*: we define the domain models here, which will be used by the other modules in the domain, presentation, and data layers.
- **data**: defines the data access layer. In this layer, we must define all the possible data sources that can provide information to the feature, such as databases, REST services, Bluetooth devices, GPS, etc. It consists of the following modules (this is the most complex layer by far):
    - *repository*: in this layer, we implement the different repositories defined in the domain layer, using the references obtained from the gateway layer. Therefore, this layer has access to:
        - domain->repository
        - domain->models
        - data->models
        - data->gateway
    - *gateway*: in this layer (similar in functionality to domain->repository), we define the interfaces that allow us to obtain data from the different data sources supported by our app.
    - *datasources*: we implement the interfaces defined in the gateway layer, obtaining data from any possible data source (local database, REST services, Bluetooth, GPS, etc.).
    - *models*: in this layer, we define the different data models of the feature, organized based on their possible origin/destination (local, remote, Bluetooth, etc.).
      Schematically, the architecture of the app (with the definition of features) would be as follows:
      ![MVVM Feature Layered Arch.png](art%2FMVVM%20Feature%20Layered%20Arch.png)
## Feature Definition and Creation
This template has the particularity that it is organized in such a way that each feature is completely independent of others, with each feature having its own MVVM architecture pattern. Manually defining this number of modules for each feature would be a serious problem, as it is not a simple pattern to deploy.
To address the problem described in the previous paragraph, this template implements a Gradle task called **createFeature**, which exposes a couple of parameters to Gradle:
- **featureName**: with this parameter, we indicate the name of the new feature. It is common to name each new feature following the pattern **feature:<featureName>**, where the variable part is <featureName>. For example, for a splash feature, we would use **feature:splash**.
- **featurePackage**: with this parameter, we define the base package for the feature. Each module of the feature will have its own package, but they will all share this base package as a common part.
### Creating a New Feature
To create a new feature, you only need to open a console in the development environment (Android Studio) and execute the following command (to create the splash feature in this example):
bash
./gradlew createFeature --featureName="feature:splash" --featurePackage="io.github.afalabarce.mvvmfeaturedlayered.feature.splash"
Once the process is finished, you just need to synchronize your project with the new Gradle configuration, and your new feature will be available, linked as a dependency in the app module of the project.
## Configurations, Dependencies, and Implemented Technologies
This project has a set of default dependencies and technologies configured, allowing for a quick start of a project, as each layer only defines what it needs, avoiding redundancies or unnecessary dependencies. Therefore, looking at each layer, we have the following implemented technologies/dependencies:
- **version catalog**: for efficient dependency management.
- **Dagger-Hilt**: as the dependency injection system.
- **Unit Testing with jUnit5**: the development by Marcel Schnelle is implemented, as well as the extension functions and infix from Kluent.
- **Coroutines**: the entire project is implemented with Coroutines at all levels.
- **Flow**: since we are using Coroutines, why not use Flows as well, as they are the new standard? This means that no dependencies for LiveData have been added.
- **Jetpack Datastore**: the new reactive preference storage system for Android apps, replacing the outdated and synchronous SharedPreferences.
- **Room**: the de facto ORM for Android, so there is not much more to add.
- **Retrofit**: the de facto standard for accessing REST APIs in Android.
- **Jetpack Compose**: in the UI layers, both for each feature and the app module, everything is prepared to use the new declarative view system (in its version for Material3), although support for classic views is also provided.
- **Coil**: a set of elements that allows for reactive and asynchronous image loading, fully adapted to Compose.
- **Paging3**: an API that facilitates pagination processes in APIs (e.g., very fast development of infinite scroll with Compose).
- **Jetpack Compose Navigation**: the dependencies are added to correctly execute navigation between screens developed with Jetpack Compose.
- **Biometric Authentication**: with these dependencies, we can manage biometric authentication processes using the Android biometric subsystem.
- **Various Accompanist libraries and dependencies**: the Accompanist libraries are alpha state libraries that Google is testing to potentially integrate into the standard APIs.
    - *Permission Handling*: a much more convenient way to manage user permissions. Fully integrated with Jetpack Compose.
    - **SwipeToRefresh**: allows for the classic pull-to-refresh with Compose.
## Tips
Since these templates are empty and do not have any code, as a suggestion, you could use parts of my [MVVM Template](https://github.com/afalabarce/MVVMProject-Hilt), which includes initialized elements for creating connections to REST APIs, preparing Room databases with migrations, etc.
## Conclusions
This template is an exercise in designing a complex project, aiming to create a highly scalable and modular project in which different development teams of an indeterminate scale could participate.
I hope that, at the very least, it can serve as a didactic element for practicing and/or improving the different skills of any developer who considers studying or using it.