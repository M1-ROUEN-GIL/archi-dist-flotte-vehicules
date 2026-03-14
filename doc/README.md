graph TD
    %% Définition des acteurs
    Client([📱 Frontend / App Mobile])
    Gateway[🌐 API Gateway GraphQL]
    Kafka{{🔀 Apache Kafka Event Bus}}

    %% Définition des Services et de leurs Bases
    subgraph "Domaine Métier (Microservices isolés)"
        Vehicules[🚌 Service Véhicules] --- DB_V[(PostgreSQL)]
        Conducteurs[👨‍✈️ Service Conducteurs] --- DB_C[(PostgreSQL)]
        Maintenance[🔧 Service Maintenance] --- DB_M[(PostgreSQL)]
        Localisation[📍 Service Localisation] --- DB_L[(TimescaleDB)]
        Evenements[⚠️ Service Événements] --- DB_E[(PostgreSQL/Redis)]
    end

    %% Flux Synchrones (Requêtes REST/GraphQL)
    Client -- "Requête GraphQL" --> Gateway
    Gateway -- "API REST" --> Vehicules
    Gateway -- "API REST" --> Conducteurs
    Gateway -- "API REST" --> Maintenance
    Gateway -- "API REST" --> Evenements
    Gateway -- "gRPC/REST" --> Localisation

    %% Flux Asynchrones (Événements Kafka)
    Localisation -- "Publie position GPS" --> Kafka
    Vehicules -- "Publie statut" --> Kafka
    Maintenance -- "Publie fin de révision" --> Kafka
    
    Kafka -- "Écoute positions GPS" --> Evenements
    Kafka -- "Écoute pannes" --> Maintenance
    Kafka -- "Écoute fins de révision" --> Vehicules
