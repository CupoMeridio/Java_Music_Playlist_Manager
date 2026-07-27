# 🎓 Guida alla Presentazione del Contributo Personale per l'Esame

Questa traccia è pensata per aiutarti a esporre in modo chiaro, accademico ed efficace il tuo contributo al progetto **Java Music Playlist Manager** davanti alla commissione d'esame. I docenti di *Software Architecture* o *Software Engineering* apprezzano particolarmente il rigore terminologico, la giustificazione delle scelte architetturali (es. i Design Pattern) e l'applicazione dei principi SOLID.

---

## 📌 1. Introduzione ed Impatto Generale (La "Big Picture")
* **Obiettivo**: Mostrare subito la rilevanza del tuo ruolo all'interno del team.
* **Cosa dire**:
  > *"All'interno del gruppo di lavoro, ho coordinato e sviluppato una porzione significativa dell'intero ecosistema software, facendomi carico di circa il **40% delle attività di sviluppo complessive** (71 commit su 177). Il mio lavoro si è esteso verticalmente lungo l'intero stack applicativo: dalla configurazione iniziale dell'ambiente di sviluppo, all'ingegnerizzazione del core di riproduzione audio, fino alla ristrutturazione architetturale della UI in ottica di modularità e disaccoppiamento."*

---

## 🛠️ 2. Scelte Architetturali e Design Pattern (Il punto più critico per l'esame)
I docenti valuteranno la tua capacità di strutturare il codice. Concentrati su questi quattro pilastri che hai implementato o rifattorizzato:

### A. Factory Pattern (Creazione dei Modelli)
* **Concetto**: Creazione controllata degli oggetti del dominio.
* **Come esporlo**:
  > *"Per disaccoppiare la creazione degli oggetti del modello di dominio dalla loro implementazione concreta e facilitare l'estendibilità futura, ho introdotto il **Factory Pattern** per istanziare entità chiave come brani e playlist, riducendo le dipendenze dirette fra le classi."*

### B. Observer Pattern (Reattività e Aggiornamento UI)
* **Concetto**: Sincronizzazione dinamica dello stato.
* **Come esporlo**:
  > *"Ho revisionato e corretto il meccanismo di notifica del pattern **Observer** del modello. Mancavano delle notifiche cruciali dal modello verso le viste, il che impediva l'aggiornamento coerente dell'interfaccia utente al variare dello stato interno della libreria musicale. Ripristinando il corretto flusso di notifiche, lo stato della UI rispecchia sempre fedelmente lo stato del Model."*

### C. Design Pattern Composite (Gestione delle Playlist)
* **Concetto**: Strutture gerarchiche e principio di segregazione delle interfacce.
* **Come esporlo**:
  > *"Abbiamo lavorato sulla gestione delle playlist tramite il pattern **Composite** (permettendo a playlist di contenere altre playlist o brani tramite un'interfaccia comune `Playable`). Nelle fases finali, per garantire una migliore manutenibilità e aderenza al principio di segregazione delle responsabilità, ho effettuato un refactoring per **separare le operazioni di playlist mutabili (scrittura) dalla classe base Playlist**, isolando i comportamenti di modifica della struttura dati."*

### D. Disaccoppiamento e Clean Architecture (Separation of Concerns)
* **Concetto**: Riduzione del debito tecnico e applicazione del Single Responsibility Principle (SRP).
* **Come esporlo**:
  > *"Inizialmente, la classe [PrimaryViewController.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/PrimaryViewController.java) soffriva del problema del 'God Object', accentrando troppa logica di gestione dell'interfaccia. Ho eseguito un importante refactoring estraendo le responsabilità in tre servizi specializzati dedicati:*
  > 1. *[ContextMenuManager.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/services/ContextMenuManager.java) per la gestione dei menu contestuali.*
  > 2. *[PlaylistDialogService.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/services/PlaylistDialogService.java) per le finestre modali di creazione/modifica.*
  > 3. *[LibrarySearchService.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/services/LibrarySearchService.java) per le logiche di filtraggio e ricerca.*
  > *Questo ha drasticamente ridotto l'accoppiamento e facilitato la testabilità del codice."*

---

## 🎵 3. Funzionalità Core Sviluppate
Spiega come hai tradotto i requisiti funzionali in codice funzionante.

### A. Integrazione Riproduzione Audio Reale (Task 2.4.1)
* **Come esporlo**:
  > *"Ho guidato la transizione del sistema da un motore di riproduzione fittizio (mock) a un player audio reale basato su file fisici. Ho implementato la gestione degli stati di playback nel player (Play, Pause, Stop, Skip, Previous) interfacciandomi direttamente con il file system."*

### B. Parser Metadati dei File Audio
* **Come esporlo**:
  > *"Per automatizzare l'inserimento dei dati dei brani ed evitare errori da parte dell'utente, ho introdotto una libreria di parsing dei metadati dei file multimediali, sostituendo le Java Media API native che presentavano problemi di stabilità su determinati formati e sistemi operativi. Ora l'applicazione estrae automaticamente titolo, artista, album, durata e copertina del file audio importato."*

### C. Homepage e Dashboard delle Statistiche
* **Come esporlo**:
  > *"Ho progettato e sviluppato la schermata Home dell'applicazione, che funge da dashboard analitica mostrando all'utente informazioni aggregate ricavate in tempo reale dal modello: i brani più riprodotti, le playlist più ascoltate e le metriche sulle dimensioni delle playlist."*

---

## 🎨 4. Esperienza Utente (UX), Grafica e Temi
I docenti apprezzano quando l'ingegneria del software si traduce in un prodotto finito curato e utilizzabile.

* **Come esporlo**:
  > *"Ho curato l'estetica e l'usabilità dell'applicazione. Ho strutturato un sistema completo di temi dinamici intercambiabili a runtime dall'utente, ripulendo e ottimizzando i file CSS. Per l'iconografia dell'interfaccia ho integrato le librerie Ikonli e FontAwesome, eliminando asset pesanti o obsoleti. Inserendo bug-fix grafici, ho risolto le incongruenze di stile tra componenti native di JavaFX (come ComboBox e CheckComboBox) garantendo una resa visiva omogenea su tutti i temi."*

---

## 📄 5. Gestione del Ciclo di Vita del Progetto e Documentazione
Mostra che hai seguito una metodologia controllata (es. Agile/Scrum).

* **Come esporlo**:
  > *"Oltre allo sviluppo software, ho partecipato attivamente alla stesura della documentazione di progetto e dei report di retrospettiva per ciascuno dei tre Sprint. Ho allineato i diagrammi UML delle classi e dei casi d'uso in PlantUML alle variazioni architetturali introdotte in corso d'opera, documentando formalmente i cambiamenti di design concordati durante le peer-review."*

---

## 💡 Suggerimenti per l'esposizione orale:
1. **Usa parole chiave ingegneristiche**: Parla di *disaccoppiamento*, *coesione*, *riduzione del debito tecnico*, *principio di singola responsabilità (SRP)*, *information hiding*.
2. **Usa l'approccio Problema ➡️ Soluzione**: Per ogni punto, spiega brevemente la criticità iniziale (es. *"Prima il PrimaryViewController conteneva più di 2000 righe di codice ed era difficile da manutenere..."*) e come la tua soluzione ha risolto il problema (*"...quindi ho estratto la logica nei Service dedicati riducendolo a poche righe leggibili"*).
3. **Fai riferimento al codice reale**: Se ti chiedono dettagli, puoi mostrare le classi estratte o il funzionamento dell'Observer all'interno di [PlaybackManager.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/model/PlaybackManager.java) o [PrimaryViewController.java](file:///C:/Users/cupom/OneDrive/Documenti/Cartella Vittorio/Software Architecture Design/Java_Music_Playlist_Manager/src/main/java/it/unisa/java_music_playlist_manager/PrimaryViewController.java).
