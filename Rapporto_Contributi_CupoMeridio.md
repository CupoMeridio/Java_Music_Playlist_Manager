# 📊 Report di Analisi Contributi Git - Utente **CupoMeridio**

Questo documento fornisce un'analisi dettagliata e strutturata dell'apporto fornito dall'utente **CupoMeridio** (Vittorio Postiglione) allo sviluppo del progetto **Java Music Playlist Manager**.

## 📈 Riepilogo Metrico ed Impatto Generale

| Metrica | Valore | Note / Percentuale su Totale |
| :--- | :--- | :--- |
| **Commit Totali Repository** | 177 | 100% |
| **Commit CupoMeridio** | 71 | **40.11%** del repository |
| **Righe Aggiunte** | +16,066 | Codice e documentazione inseriti |
| **Righe Rimosse** | -7,387 | Refactoring e rimozione ridondanze |
| **File Unici Modificati** | 168 | Dalle classi Java ai fogli di stile CSS e doc |
| **Periodo Attività** | 19 Maggio 2026 - 17 Luglio 2026 | ~2 mesi di sviluppo attivo |

## 🗺️ Distribuzione dell'Attività per Fasi (Sprint)

- **Pre-Game (Setup e Inizializzazione)**: **8 commit** (+297 / -2)
- **Sprint 1 (Riproduzione, Model & View)**: **15 commit** (+5,050 / -2,097)
- **Sprint 2 (Home, Ricerca, Observer & Pattern)**: **9 commit** (+1,363 / -1,607)
- **Sprint 3 (Grafica, Temi, Tag & Composite)**: **28 commit** (+6,673 / -1,370)
- **Post-Consegna (Rifattorizzazione UI & Stabilità)**: **11 commit** (+2,683 / -2,311)

## 🔑 Aree Chiave di Contributo Tecnico

L'apporto dell'utente si è concentrato su diverse macro-aree di ingegnerizzazione del software:

### 1. Ingegnerizzazione dell'Audio e Gestione Metadati
- **Integrazione della Riproduzione Reale**: Sostituite le logiche mock con riproduzione audio effettiva dei file musicali (mp3/wav) (Task 2.4.1).
- **Gestione Metadati Avanzata**: Sostituita la libreria standard (Java Media API) con una libreria di terze parti molto più solida e stabile, ottimizzando l'estrazione automatica di artista, album, copertina, e durata dei brani all'atto dell'importazione.

### 2. Architettura del Software e Pattern di Design
- **Factory Pattern**: Introduzione di Factory per la creazione organizzata degli oggetti del modello, migliorando la modularità e l'estensibilità.
- **Observer Pattern**: Correzione del meccanismo di notifica agli Observer per garantire il corretto aggiornamento in tempo reale dell'interfaccia grafica in seguito a modifiche dello stato o dei dati.
- **Design Pattern Composite**: Gestione e refactoring di Playable/Playlist per supportare strutture ad albero, con successivi isolamenti di operazioni di playlist mutabili per disaccoppiare logiche di scrittura e lettura.
- **Disaccoppiamento UI-Model**: Estrazione di logiche complesse dal controller principale ([PrimaryViewController.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/PrimaryViewController.java)) verso classi di servizio specializzate ([ContextMenuManager.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/services/ContextMenuManager.java), [PlaylistDialogService.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/services/PlaylistDialogService.java), [LibrarySearchService.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/services/LibrarySearchService.java)), garantendo la conformità al principio di singola responsabilità (SRP).

### 3. Interfaccia Grafica (UI), Temi ed Usabilità (UX)
- **Schermata Home & Statistiche**: Creazione da zero di una dashboard con riepilogo dati (tracce più riprodotte, playlist più ascoltate, statistiche generali).
- **Gestione dei Temi**: Implementazione di un sistema di temi dinamici e pulizia del codice CSS per l'intero applicativo.
- **Integrazione Icone**: Adozione delle librerie *Ikonli* e *FontAwesome* per una veste grafica professionale.
- **Componenti Custom e Bug Fix**: Risoluzione di difetti di stile dei controlli UI (ComboBox, CheckComboBox), pulizia dei form all'annullamento dell'aggiunta, e fix sulla barra di ricerca nella sezione playlist.

### 4. Documentazione del Progetto
- **Redazione Report di Sprint**: Gestione e allineamento della documentazione di progetto per gli Sprint 1, 2 e 3.
- **Modellazione UML**: Creazione e revisione dei diagrammi UML delle classi e dei casi d'uso (es. diagrammi dello Sprint 3).

## 🗂️ Analisi dei File Più Modificati

Di seguito sono elencati i 15 file in cui l'utente ha registrato la maggiore quantità di modifiche ed interazioni:

| Nome File | Modifiche (Commit) | Righe Aggiunte | Righe Rimosse | Ruolo / Contributo |
| :--- | :---: | :---: | :---: | :--- |
| [PrimaryViewController.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/PrimaryViewController.java) | 32 | +2603 | -2556 | Codice Sorgente Java |
| [Playlist.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/model/Playlist.java) | 12 | +276 | -111 | Codice Sorgente Java |
| [module-info.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/module-info.java) | 12 | +31 | -3 | Codice Sorgente Java |
| [PlayerController.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/PlayerController.java) | 11 | +640 | -212 | Codice Sorgente Java |
| [AddTrackController.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/AddTrackController.java) | 10 | +976 | -478 | Codice Sorgente Java |
| [primaryView.fxml](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/resources/fxml/primaryView.fxml) | 8 | +222 | -52 | Interfaccia FXML (JavaFX) |
| [pom.xml](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/pom.xml) | 8 | +195 | -5 | Configurazione Maven / NetBeans |
| [PlaybackManager.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/model/PlaybackManager.java) | 7 | +542 | -92 | Codice Sorgente Java |
| [README.md](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/README.md) | 7 | +398 | -9 | Documentazione Markdown |
| [Track.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/model/Track.java) | 7 | +218 | -82 | Codice Sorgente Java |
| [TagPredefined.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/model/TagPredefined.java) | 7 | +60 | -47 | Codice Sorgente Java |
| [addTrackView.fxml](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/resources/fxml/views/addTrackView.fxml) | 6 | +270 | -320 | Interfaccia FXML (JavaFX) |
| [primaryview.css](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/resources/styles/themes/phantom-thief/primaryview.css) | 6 | +342 | -31 | Foglio di Stile CSS |
| [App.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/App.java) | 6 | +108 | -71 | Codice Sorgente Java |
| [SidebarController.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/SidebarController.java) | 6 | +100 | -50 | Codice Sorgente Java |

## 📅 Registro Storico dei Commit di CupoMeridio

Cronologia completa di tutti i commit attribuiti all'utente CupoMeridio (in ordine inverso di data):

| Data | Hash | Messaggio | + / - |
| :--- | :---: | :--- | :---: |
| 2026-07-17 | `56aff14` | Modificato aspetto grafico di un tema (sperimentale) e pulizia codice css | +271/-72 |
| 2026-07-10 | `9278db1` | Rimozione log di debug e pulizia del codice | +0/-67 |
| 2026-07-10 | `a3d95c7` | Correzioni CSS temi: colore accento kawaii, border-radius thumb slider e allineamento border-radius globale | +37/-26 |
| 2026-07-09 | `9deadb1` | Risolte incongruenze di stile tra ComboBox e CheckComboBox nella vista addtrack | +109/-144 |
| 2026-07-06 | `c9aa48e` | Fix bug: Inizializza correttamente la vista Musica all'avvio | +2/-1 |
| 2026-07-06 | `3ab4761` | Risolte inconsistenze di stile i tutti i temi | +135/-0 |
| 2026-07-06 | `917e4a0` | Merge branch 'post-consegna' of https://github.com/CupoMeridio/Java_Music_Playlist_Manager into post-consegna | +1029/-892 |
| 2026-07-06 | `6180ea8` | Pulizia codice css | +5/-151 |
| 2026-06-29 | `2fccd96` | Completato refactoring: estrazione di ContextMenuManager, PlaylistDialogService e LibrarySearchService | +257/-235 |
| 2026-06-29 | `b399ff5` | refactor: estrai logiche UI dal PrimaryViewController in classi dedicate | +606/-613 |
| 2026-06-29 | `de33379` | Refactor della gestione delle viste e aggiunta UI per tag e playlist | +232/-110 |
| 2026-06-18 | `e846258` | Correzioni varie alla documentazione | +27/-17 |
| 2026-06-18 | `1ca3c0f` | Merge branch 'master' of https://github.com/CupoMeridio/Java_Music_Playlist_Manager | +0/-0 |
| 2026-06-18 | `f6a5e9e` | aggiunta documentazione relativa allo sprint 3 | +29/-0 |
| 2026-06-18 | `b638d8b` | aggiunto css mancante per la schermata di aggiunta e modifica playlist | +320/-15 |
| 2026-06-18 | `7fb0abc` | corretti due bug: - la barra di ricerca non era funzionante nella sezione playlist - il form di agguinta e modifica brani non veniva pulito correttamente nel caso di una modifica annullata | +9/-2 |
| 2026-06-18 | `5fa1803` | correzioni stile css | +174/-40 |
| 2026-06-18 | `4a8d886` | correzioni css | +44/-5 |
| 2026-06-17 | `e39c83b` | Merge pull request #6 from CupoMeridio/composite/mutable-playlist | +55/-66 |
| 2026-06-17 | `31c0f94` | Correzioni all'interfaccia grafica | +55/-66 |
| 2026-06-17 | `0fc11b1` | Delete inizio_sprint_3_UML.puml | +0/-265 |
| 2026-06-17 | `2d57445` | Merge pull request #5 from CupoMeridio/composite/mutable-playlist | +244/-32 |
| 2026-06-17 | `8ff787e` | Inserito css mancante | +19/-14 |
| 2026-06-17 | `0979250` | Funzionalità: espansione del sistema di tag, miglioramento delle opzioni di genere, aggiunta della documentazione del progetto | +225/-18 |
| 2026-06-17 | `6b77626` | Add files via upload | +0/-0 |
| 2026-06-17 | `2c01276` | Add files via upload | +0/-0 |
| 2026-06-17 | `f302cee` | Rename Sprint 3 Retrospective Report-2.pdf to Sprint 3 Retrospective Report.pdf | +0/-0 |
| 2026-06-17 | `2c3a353` | Add files via upload | +0/-0 |
| 2026-06-17 | `6efed8d` | Delete docs/3 - Third Sprint/Sprint 3 Retrospective Report.pdf | +0/-0 |
| 2026-06-17 | `d554de2` | Add files via upload | +0/-0 |
| 2026-06-17 | `d213b29` | Add files via upload | +0/-0 |
| 2026-06-17 | `2752d9e` | Merge pull request #4 from CupoMeridio/composite/mutable-playlist | +14/-25 |
| 2026-06-17 | `a019038` | Separate mutable playlist operations from Playlist | +14/-25 |
| 2026-06-17 | `ad595cd` | aggiunto nuovo tema | +715/-11 |
| 2026-06-16 | `aaaedf8` | Gestione delle icone dell'app attraverso Ikonli e awesome font | +379/-84 |
| 2026-06-16 | `ff0311f` | Implementati temi | +4033/-293 |
| 2026-06-15 | `e33fb22` | Merge pull request #3 from CupoMeridio/fix-metadati | +157/-84 |
| 2026-06-15 | `a5795d0` | Apportate modifiche alla gestione dei metadati dei brani grazie all'uso di una nuova libreria che funziona meglio della java media api | +157/-84 |
| 2026-06-15 | `1927ad6` | Aggiunto file di salvattaggio al gitignore e effettuata pulizia della cache del git. | +3/-224 |
| 2026-06-12 | `349feb6` | Implementata funzionalità della barra di ricerca | +95/-4 |
| 2026-06-12 | `47db64e` | Merge origin/master | +157/-1 |
| 2026-06-12 | `9010c63` | -.- | +5/-5 |
| 2026-06-12 | `41c8550` | Aggiunta schermata home per riepilogo statische quali: - Brani più riprodotti - Playlist più riprodotte - Playlist con più canzoni in futuro si possono aggiungere anche altre tabelle | +437/-9 |
| 2026-06-11 | `83888c2` | Aggiunto toString mancante alla classe playlist che veniva utilizzato dall'interfaccia grafica | +6/-0 |
| 2026-06-11 | `bef9b80` | Aggiunto README per i documenti del secondo sprint | +37/-0 |
| 2026-06-11 | `6611d54` | Mancavano delle notifiche agli observer | +13/-0 |
| 2026-06-11 | `5c1cb01` | Merge pull request #2 from CupoMeridio/rafactormattia | +612/-569 |
| 2026-06-11 | `00e15c8` | Implementazione pattern factpry e refactring del progetto. | +1/-1019 |
| 2026-06-08 | `fbaeae0` | Aggiunto readme relativo alla documentazione inserita per lo sprint 1 | +31/-0 |
| 2026-06-07 | `722e384` | rimosso pezzettino di documentazione duplicato | +3/-5 |
| 2026-06-07 | `9f60191` | Merge branch 'master' of https://github.com/CupoMeridio/Java_Music_Playlist_Manager | +616/-30 |
| 2026-06-07 | `7f38794` | piccolo fix + allineamento documentazione del progetto con struttura aggiornata | +1125/-713 |
| 2026-06-07 | `11da6ae` | Merge origin/master | +27/-11 |
| 2026-06-07 | `d9a425a` | Modifiche al form di aggiunta brano: - Il bottone per aggiungere il file audio è stato spostato in cima - i campi del form sono stati disbilitati fino a quando non viene effettivamente   selezionato un file - Rimosso il campo sulla durata in quanto corrisopnde a un dato che non   deve essere fonrito dall'utente ma deve essere ricavato in automatico   dal sistema | +33/-17 |
| 2026-06-07 | `3adf474` | Merge pull request #1 from CupoMeridio/feat/audio-reale | +817/-363 |
| 2026-06-07 | `c3ea58a` | Merge branch 'master' into feat/audio-reale | +219/-181 |
| 2026-06-07 | `620ae8e` | Completamento del task 2.4.1: Integrazione riproduzione audio reale e autocompilazione metadati | +823/-321 |
| 2026-06-05 | `392aa12` | Aggiunto tasto di aggiunta brano a coda di produzione | +54/-5 |
| 2026-06-05 | `202bc26` | Correzioni applicate a seguito dell'implementazione del composite e di playable emerse nel peer to peer review: - aggiunta funzionalità di salto al file riproducibile precedente e controlli UI - Implementata la logica di riproduzione per l'elemento precedente tra tutti gli stati di playback. - Aggiunti i pulsanti UI per il doppio salto (precedente/successivo) nella barra del player. | +352/-136 |
| 2026-06-03 | `95f8f87` | Revert dell'implementazione di LibraryService | +16/-106 |
| 2026-06-03 | `46a4fc0` | Rifattorizzazione gestione media e aggiunta del service per le interazioni tra interfaccia grafica e modelli - Rimosso l'interfaccia deprecata Playable e il relativo pattern composite per le playlist annidate. - Rinomati i metodi generici add/remove in addTrack/removeTrack specifici per le tracce in tutta la codebase. - Sostituito il salvataggio dei dati in Playlist da Set<Playable> a ArrayList<Track>, mantenendo comunque la logica di prevenzione dei duplicati. - Aggiunto il service LibraryService per disaccoppiare l'interfaccia utente (UI) dall'accesso diretto al singleton del modello di dominio. - Semplificata la logica di riproduzione in StoppedState rimuovendo un controllo null ridondante e migliorando l'output dei log. - Rifattorizzato PrimaryViewController per utilizzare LibraryService al posto delle chiamate dirette a Library. - Rimossi i metodi inutilizzati play() e i metodi add/remove dalla classe Track. | +177/-142 |
| 2026-06-02 | `2dd5b26` | Implementata gestione grafica delle playlist | +226/-8 |
| 2026-05-29 | `796a796` | feat: setup iniziale progetto, configurazione ambiente e overhaul interfaccia | +531/-59 |
| 2026-05-26 | `fc4eae0` | Add Phase 1 project documentation | +31/-0 |
| 2026-05-26 | `22aea7b` | Add files via upload | +0/-0 |
| 2026-05-26 | `e8e77c7` | Add files via upload | +0/-0 |
| 2026-05-25 | `b45ff7b` | Delete docs/0 - Pre Game/noneliminare.txt | +0/-1 |
| 2026-05-25 | `9624648` | Delete docs/prova.txt | +0/-1 |
| 2026-05-25 | `3e0434f` | Create noneliminare.txt | +1/-0 |
| 2026-05-25 | `23f57ca` | Create prova.txt | +1/-0 |
| 2026-05-19 | `0a13d77` | Configurazione iniziale del progetto Netbeans | +264/-0 |

---
*Generato in automatico dal sistema di analisi di Antigravity per la revisione del portfolio di sviluppo.*