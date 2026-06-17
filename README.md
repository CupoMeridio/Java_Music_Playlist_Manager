# Java Music Playlist Manager

**Java Music Playlist Manager** è un'applicazione desktop in Java per la gestione e la riproduzione di una libreria musicale composta da brani locali presenti sul PC.

Il progetto è stato realizzato per l'esame di **Software Architecture Design** del corso di laurea in Informatica presso l'Università degli Studi di Salerno. Oltre alle funzionalità dell'applicazione, il punto centrale del lavoro è il percorso ingegneristico seguito: progettazione agile, gestione del team, definizione di task e user story, documentazione tecnica, scelte architetturali e applicazione di pattern di design.

## Panoramica

L'applicazione permette di:

- importare brani musicali locali dal filesystem;
- leggere metadati audio come titolo, artista, album, genere, anno e durata;
- gestire una libreria musicale personale;
- creare playlist manuali;
- generare playlist automatiche per genere, tag o anno;
- modificare, rinominare ed eliminare tracce e playlist;
- riprodurre brani e playlist tramite una coda di riproduzione;
- applicare strategie di riproduzione sequenziale, casuale e ripetuta;
- consultare statistiche su brani e playlist più riprodotte;
- cambiare tema grafico dell'interfaccia.

Il software è pensato per lavorare su file musicali locali e non richiede servizi esterni o piattaforme di streaming.

## Focus del progetto

Questo repository non contiene solo un'applicazione funzionante, ma soprattutto la documentazione del processo di sviluppo.

La cartella [`docs/`](docs/) raccoglie, sprint dopo sprint, gli artefatti prodotti durante il corso:

- Product Backlog iniziale;
- Relazione Tecnico-Metodologica;
- Sprint Planning;
- Sprint Backlog;
- Burndown Chart;
- Sprint Review;
- Sprint Retrospective;
- presentazioni di progetto;
- diagrammi e documentazione architetturale.

Questa documentazione è parte integrante del progetto, perché descrive le scelte di progettazione, l'organizzazione del lavoro di gruppo e l'evoluzione dell'architettura software.

## Documentazione

La documentazione è organizzata per fase e sprint:

```text
docs/
├── 0 - Pre Game/
│   ├── Initial_Product_Backlog.pdf
│   ├── Relazione Tecnico-Metodologica.pdf
│   ├── Sprint_Planning_1.pdf
│   └── Presentazione SAD_Gruppo1.pdf
├── 1 - First Sprint/
│   ├── 1sprint_review_retrospective.pdf
│   ├── BurndownChartsFirstSprint_Gruppo01.pdf
│   ├── Presentazione First Sprint Gruppo01.pdf
│   └── Sprint_Planning-2.pdf
├── 2 - Second Sprint/
│   ├── Sprint Backlog - Sprint Backlog 2.pdf
│   ├── Burndown Charts Seconda Sprint.pdf
│   ├── Sprint 2 Retrospective Report.pdf
│   ├── Presentazione2ªSprintGruppo1.pdf
│   ├── DiagrammaDelleClassi.jpeg
│   └── Sprint 3 Planning Report.pdf
└── 3 - Third Sprint/
```

Per una lettura guidata, si consiglia di iniziare da:

1. [`docs/0 - Pre Game/README.md`](docs/0%20-%20Pre%20Game/README.md)
2. [`docs/1 - First Sprint/README.md`](docs/1%20-%20First%20Sprint/README.md)
3. [`docs/2 - Second Sprint/README.md`](docs/2%20-%20Second%20Sprint/README.md)

## Architettura software

Il progetto segue una separazione tra interfaccia utente, logica applicativa e modello di dominio.

La parte principale dell'applicazione è implementata in Java con JavaFX, mentre il modello di dominio contiene le entità e i servizi principali:

- `Library`, che rappresenta la libreria musicale;
- `Track`, che rappresenta un brano;
- `Playlist`, `ManualPlaylist` e playlist automatiche;
- `PlaybackManager`, che coordina la riproduzione;
- `LibraryDAO` e `JsonLibraryDAO`, che gestiscono la persistenza su file JSON.

Sono stati applicati diversi pattern architetturali e di design, tra cui:

- **Singleton**, per componenti con istanza unica come `Library`, `PlaybackManager`, `UndoManager` e `ThemeManager`;
- **Observer**, per aggiornare automaticamente la UI quando cambiano libreria o stato di riproduzione;
- **Command**, per gestire operazioni annullabili come aggiunta, modifica e rimozione di tracce e playlist;
- **State**, per modellare i diversi stati del player: stopped, playing e paused;
- **Strategy**, per gestire diverse strategie di avanzamento nella coda;
- **Composite**, per trattare tracce singole e playlist come elementi riproducibili;
- **Factory Method**, per la creazione di playlist manuali e automatiche.

## Tecnologie utilizzate

- Java 21
- Maven
- JavaFX
- ControlsFX
- JAudiotagger, per la lettura dei metadati audio
- Jackson, per la serializzazione e deserializzazione JSON
- JUnit 5, per i test automatici

## Requisiti

Per compilare ed eseguire il progetto è necessario avere installato:

- JDK 21 o compatibile;
- Maven;
- un IDE Java, ad esempio NetBeans, IntelliJ IDEA o Eclipse.

## Esecuzione

Dalla root del progetto:

```bash
mvn clean compile
mvn javafx:run
```

In alternativa, il progetto può essere aperto direttamente da NetBeans ed eseguito tramite la configurazione Maven/JavaFX presente nel progetto.

## Test

Per eseguire la suite di test:

```bash
mvn test
```

I test coprono diverse parti del modello, tra cui:

- gestione della libreria;
- aggiunta e rimozione di tracce;
- playlist manuali e automatiche;
- strategie di riproduzione;
- persistenza JSON;
- comportamenti principali del player.

## Struttura del repository

```text
src/
├── main/
│   ├── java/
│   │   └── it/unisa/java_music_playlist_manager/
│   │       ├── App.java
│   │       ├── PrimaryViewController.java
│   │       ├── HomeController.java
│   │       ├── SidebarController.java
│   │       ├── PlayerController.java
│   │       ├── ThemeManager.java
│   │       └── model/
│   └── resources/
│       ├── fxml/
│       ├── styles/
│       └── images/
└── test/
    └── java/
        └── it/unisa/java_music_playlist_manager/
```

I file principali dell'interfaccia sono definiti in FXML nella cartella [`src/main/resources/fxml/`](src/main/resources/fxml/), mentre gli stili e i temi grafici sono organizzati in [`src/main/resources/styles/`](src/main/resources/styles/).

## Persistenza

L'applicazione salva la libreria musicale su file JSON, in modo da mantenere traccia di:

- brani importati;
- playlist create;
- metadati;
- tag;
- contatori di riproduzione;
- preferenze di tema.

I dati di salvataggio vengono generati localmente durante l'esecuzione dell'applicazione.