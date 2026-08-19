# Java Music Playlist Manager

**Java Music Playlist Manager** è un'applicazione desktop in Java per la gestione e la riproduzione di una libreria musicale composta da brani locali presenti sul PC.

Il progetto è stato realizzato per l'esame di **Software Architecture Design** del corso di laurea in Ingegneria Informatica presso l'Università degli Studi di Salerno. Oltre alle funzionalità dell'applicazione, il punto centrale del lavoro è il percorso ingegneristico seguito: progettazione agile, gestione del team, definizione di task e user story, documentazione tecnica, scelte architetturali e applicazione di pattern di design.

---

## Download ed Esecuzione Rapida

Per utilizzare direttamente l'applicazione senza compilare il codice sorgente, è possibile scaricare l'ultima versione disponibile dalla sezione [**GitHub Releases**](../../releases):

- **Versione Portabile per Windows (`.zip`)**: include l'eseguibile `Java-Music-Playlist-Manager.exe` e il runtime Java integrato. È sufficiente estrarre l'archivio ed eseguire il file `.exe`, senza dover installare Java sul sistema.
- **Installer Windows (`.msi`)**: pacchetto di installazione standard che installa l'applicazione e configura i collegamenti su Desktop e Menu Start.
- **File JAR Eseguibile (`.jar`)**: singolo archivio multipiattaforma avviabile con doppio clic o tramite comando `java -jar` (richiede Java 25 o superiore installato).

---

## Panoramica

L'applicazione permette di:

- importare brani musicali locali dal filesystem;
- leggere metadati audio come titolo, artista, album, genere, anno e durata;
- gestire una libreria musicale personale;
- creare playlist manuali con riordinamento drag & drop;
- generare playlist automatiche per genere, tag o anno;
- modificare, rinominare ed eliminare tracce e playlist con supporto all'annullamento delle operazioni (Undo/Redo);
- riprodurre brani e playlist tramite una coda di riproduzione;
- applicare strategie di riproduzione sequenziale, casuale e ripetuta;
- consultare statistiche su brani e playlist più riprodotte;
- cambiare tema grafico dell'interfaccia (temi moderni Light/Dark e temi Terminal ASCII CRT).

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
    ├── Sprint 3 Planning Report.pdf
    ├── relazionetecnicometodologica.pdf
    ├── Sprint Backlog Sprint 3.pdf
    ├── Burndown Charts Terza Sprint.pdf
    ├── Sprint 3 Retrospective Report.pdf
    └── Presentazione SAD 3° Sprint.pdf
```

Per una lettura guidata, si consiglia di iniziare da:

1. [`docs/0 - Pre Game/README.md`](docs/0%20-%20Pre%20Game/README.md)
2. [`docs/1 - First Sprint/README.md`](docs/1%20-%20First%20Sprint/README.md)
3. [`docs/2 - Second Sprint/README.md`](docs/2%20-%20Second%20Sprint/README.md)
4. [`docs/3 - Third Sprint/README.md`](docs/3%20-%20Third%20Sprint/README.md)

## Architettura software

Il progetto segue una netta separazione tra interfaccia utente, logica applicativa e modello di dominio.

La parte principale dell'applicazione è implementata in Java con JavaFX, mentre il modello di dominio contiene le entità e i servizi principali:

- `Library`, che rappresenta la libreria musicale;
- `Track`, che rappresenta un brano;
- `Playlist`, `ManualPlaylist` e playlist automatiche;
- `PlaybackManager`, che coordina la riproduzione;
- `AudioEngine` e `JavaFXAudioEngine`, che disaccoppiano il layer di riproduzione multimediale (Ports & Adapters / Architettura Esagonale);
- `LibraryDAO` e `JsonLibraryDAO`, che gestiscono la persistenza su file JSON.

Sono stati applicati diversi pattern architetturali e di design, tra cui:

- **Singleton**, per componenti con istanza unica come `Library`, `PlaybackManager`, `UndoManager` e `ThemeManager`;
- **Observer**, per aggiornare automaticamente la UI quando cambiano libreria o stato di riproduzione;
- **Command**, per gestire operazioni annullabili (Undo/Redo) come aggiunta, modifica e rimozione di tracce e playlist;
- **State**, per modellare i diversi stati del player: stopped, playing e paused;
- **Strategy**, per gestire diverse strategie di avanzamento nella coda;
- **Composite**, per trattare tracce singole e playlist come elementi riproducibili;
- **Factory Method**, per la creazione di playlist manuali e automatiche;
- **Dependency Inversion / Ports & Adapters**, con Composition Root esplicito in `App.java` per iniettare l'adattatore audio concreto nel Model.

## Nota didattica sull'uso dei pattern

Per scopi puramente didattici, il progetto cerca di non delegare automaticamente alcuni pattern a componenti Java o librerie che li implementano già internamente, ove questo è possibile. L'obiettivo è rendere esplicita l'applicazione dei pattern studiati nel corso, mantenendo la logica architetturale leggibile e riconducibile al codice del progetto.

Un esempio è il pattern **Observer**: sebbene JavaFX offra meccanismi basati su proprietà osservabili, binding e liste osservabili per aggiornare automaticamente alcuni controlli, il progetto implementa un observer applicativo dedicato tramite `Subject`, `Observer` e le notifiche di `Library` e `PlaybackManager`. In questo modo, quando cambiano la libreria o lo stato di riproduzione, la UI viene aggiornata attraverso una notifica esplicita gestita dal controller, invece di affidarsi completamente ai meccanismi osservabili nativi del framework.

Lo stesso criterio guida altre scelte: dove possibile, il codice evita di nascondere dietro componenti pronti all'uso pattern come Observer, Command, State o Strategy, preferendo implementazioni esplicite che rendono più chiara la struttura architetturale dell'applicazione. Questo non esclude l'uso di JavaFX per la visualizzazione, ma limita l'impiego dei suoi meccanismi osservabili al solo supporto dell'interfaccia quando non sono centrali rispetto all'obiettivo didattico del progetto.

## Tecnologie utilizzate

- **Java 25**
- **Maven** (gestione dipendenze e build lifecycle)
- **JavaFX 21** (interfaccia grafica desktop)
- **ControlsFX** (componenti UI avanzati)
- **Ikonli & FontAwesome 5** (set di icone vettoriali)
- **JAudiotagger** (lettura metadati e tag ID3 da file audio MP3, FLAC, M4A, ecc.)
- **Jackson Databind & Datatype JSR310** (serializzazione e persistenza JSON)
- **JUnit 5** (suite completa di test unitari automatici)

## Requisiti di Sviluppo

Per compilare ed eseguire il progetto dal codice sorgente è necessario avere installato:

- JDK 25 o compatibile;
- Maven;
- un IDE Java (ad esempio NetBeans, IntelliJ IDEA, Eclipse o VS Code).

## Esecuzione e Build

### Avvio in modalità sviluppo
Dalla root del progetto:

```bash
mvn clean compile
mvn javafx:run
```

In alternativa, il progetto può essere aperto ed eseguito direttamente da NetBeans (o altro IDE) tramite la configurazione Maven standard.

### Generazione del Fat JAR eseguibile
Per creare il file JAR autonomo contenente tutte le dipendenze:

```bash
mvn clean package -DskipTests
```

Il file JAR generato sarà disponibile in `target/Java-Music-Playlist-Manager.jar` ed è avviabile con:

```bash
java -jar target/Java-Music-Playlist-Manager.jar
```

## Test

Per eseguire l'intera suite di test unitari (125 test):

```bash
mvn test
```

I test coprono in modo approfondito tutte le componenti del dominio e del modello:

- gestione e integrità della libreria;
- aggiunta, modifica e cancellazione a cascata di tracce e playlist;
- playlist manuali e generatori di playlist automatiche;
- strategie di riproduzione (sequenziale, shuffle, repeat);
- persistenza e gestione file JSON corrotti;
- pattern Command e storico dell'UndoManager;
- analytics e filtri di ricerca.

## Struttura del repository

```text
├── .github/
│   └── workflows/
│       └── release.yml              # CI/CD GitHub Actions (Rolling Release "Latest")
├── docs/                            # Documentazione metodologica e report degli sprint
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── it/unisa/java_music_playlist_manager/
│   │   │       ├── App.java                      # Bootstrap applicazione e Composition Root
│   │   │       ├── Launcher.java                 # Entry point wrapper per Fat JAR
│   │   │       ├── PrimaryViewController.java    # Controller principale
│   │   │       ├── HomeController.java           # Controller dashboard e statistiche
│   │   │       ├── SidebarController.java        # Controller barra laterale
│   │   │       ├── PlayerController.java         # Controller barra di riproduzione
│   │   │       ├── AddTrackController.java       # Controller dialogo aggiunta traccia
│   │   │       ├── ThemeManager.java             # Gestore temi dinamici (Light, Dark, Terminal CRT)
│   │   │       ├── ReorderableTrackRowFactory.java
│   │   │       ├── TagCellFactory.java
│   │   │       ├── model/                        # Entità (Track, Playlist), DAO, Pattern (Command, State, Strategy)
│   │   │       └── ui/                           # Componenti UI, Adapter audio, menu contestuali
│   │   └── resources/
│   │       ├── fxml/                             # Layout grafici in FXML (View)
│   │       ├── styles/                           # File CSS e definizioni dei temi
│   │       └── images/                           # Risorse grafiche e icone applicative
│   └── test/
│       └── java/
│           └── it/unisa/java_music_playlist_manager/ # Suite di test unitari (JUnit 5)
├── nbactions.xml                                 # Configurazioni per NetBeans IDE
└── pom.xml                                       # Configurazione Maven e plugin di build
```

## Persistenza

L'applicazione salva la libreria musicale su file JSON, in modo da mantenere traccia di:

- brani importati;
- playlist create e ordine delle tracce;
- metadati audio;
- tag personalizzati;
- contatori e statistiche di riproduzione;
- preferenze di tema grafico selezionato.

I dati vengono letti all'avvio e salvati in locale automaticamente al momento della chiusura dell'applicazione, con meccanismo di salvaguardia e backup automatico in caso di file corrotto.

## Warning Noti negli IDE

Durante lo sviluppo o la compilazione del progetto, l'IDE (come VS Code, Eclipse o IntelliJ) potrebbe segnalare un warning nel file `module-info.java` relativo alla libreria `jaudiotagger`:

> *Name of automatic module 'jaudiotagger' is unstable, it is derived from the module's file name.*

Questo comportamento è noto e previsto. È dovuto al fatto che `jaudiotagger` non definisce un file `module-info.java` o un `Automatic-Module-Name` nel suo manifest, essendo stata concepita prima dell'introduzione del sistema modulare (Java 9+). Di conseguenza, Java crea un modulo automatico deducendone il nome dal nome del file `.jar`. Il warning non inficia in alcun modo la compilazione o il corretto funzionamento dell'applicazione.
