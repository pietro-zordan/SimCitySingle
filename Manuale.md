## 1. Descrizione del gioco e regole principali
SimCity Lite è un simulatore gestionale nel quale il giocatore deve sviluppare una città mantenendo un equilibrio tra popolazione, produzione economica, budget, felicità, inquinamento e disponibilità di posti di lavoro.
La città è rappresentata da una griglia di 20×20 celle. Per costruire, il giocatore seleziona una tipologia di costruzione dalla barra inferiore e successivamente sceglie una cella libera della griglia. La partita inizia con un budget di **2500 €**.

### Costruzione della città
La prima strada può essere collocata liberamente, mentre tutte le strade successive devono essere adiacenti, in senso orizzontale o verticale, a una strada già esistente. Tutte le altre costruzioni devono essere collocate accanto a una strada. Le strade, una volta costruite, non possono essere rimosse.

Le costruzioni disponibili sono:

| Costruzione        | Costo base | Funzione                                                                                             |
| ------------------ | ---------: | ---------------------------------------------------------------------------------------------------- |
| Strada             |       50 € | Permette di estendere la città e di costruire nelle celle adiacenti.                                 |
| Zona residenziale  |      200 € | Ospita la popolazione della città e richiede energia elettrica.                                      |
| Zona commerciale   |      300 € | Offre 30 posti di lavoro e produce denaro, felicità e una quantità moderata di inquinamento.         |
| Zona industriale   |      600 € | Offre 100 posti di lavoro e produce molto denaro, ma aumenta notevolmente inquinamento e infelicità. |
| Parco              |      150 € | Aumenta la felicità, riduce l’inquinamento e comporta un costo di mantenimento per la città.         |
| Centrale elettrica |      800 € | Fornisce energia alle costruzioni circostanti, ma produce inquinamento.                              |

All’inizio è quindi necessario costruire una strada, una zona residenziale e una centrale elettrica. Le zone industriali e commerciali possono essere costruite solamente se nella città sono presenti sufficienti abitanti.
L'interfaccia grafica mostra eventuali necessità o condizioni non rispettate sotto forma di pop-up che durano qualche secondo. 
### Popolazione ed energia elettrica
Ogni nuova zona residenziale parte con 10 abitanti e può raggiungere un massimo di 50. Se riceve energia, la sua popolazione cresce a ogni tick, con una velocità progressivamente maggiore. Se rimane senza corrente, la popolazione comincia invece a diminuire; quando raggiunge zero, la zona residenziale viene rimossa automaticamente dalla griglia.
Le zone industriali e commerciali prive di energia non vengono rimosse, ma smettono di produrre denaro e di influenzare felicità e inquinamento finché l’alimentazione non viene ripristinata.
Ogni centrale produce una quantità limitata di energia e può alimentare le costruzioni presenti entro un raggio di 3 caselle da essa, per un massimo di 48 celle. Le costruzioni hanno consumi differenti: le zone industriali sono quelle che richiedono più energia, seguite dalle zone commerciali e dalle zone residenziali.
Se il consumo complessivo supera la capacità della centrale, alcune costruzioni vengono scollegate. Quando torna disponibile abbastanza energia, il collegamento viene ripristinato automaticamente, dando priorità alle costruzioni con un consumo minore. Le celle non alimentate sono segnalate sulla griglia da un simbolo rosso a forma di fulmine.

### Avanzamento della simulazione
Il pulsante **Next Turn** fa avanzare la simulazione di un tick. A ogni tick vengono aggiornati:
* la crescita o la diminuzione della popolazione;
* la produzione delle zone industriali e commerciali;
* i collegamenti alle centrali elettriche;
* popolazione, economia, inquinamento e felicità;
* il budget della città;
* la durata e gli effetti degli eventi attivi.
Il valore dell’economia prodotto durante il tick viene aggiunto al budget. La produzione delle zone industriali e commerciali aumenta gradualmente nel tempo, purché gli edifici rimangano alimentati.
L’interfaccia permette inoltre di visualizzare l’andamento nel tempo di popolazione, economia, inquinamento, felicità e disoccupazione attraverso diversi grafici. Il pulsante sottostante permette di passare da un grafico a quello successivo.

### Politiche cittadine
La partita comincia con la **Standard Policy**. La politica può essere cambiata per la prima volta dopo 12 tick e, dopo ogni modifica, devono trascorrere altri 12 tick prima di poterla cambiare nuovamente.

Sono disponibili tre politiche:
* **Standard Policy:** non modifica i prezzi, l’economia o l’inquinamento.
* **Environmental Policy:** riduce del 25% l’economia e l’inquinamento complessivi. Le zone industriali costano 700 €, mentre i parchi costano solamente 100 €. È adatta a una città più pulita e orientata alla felicità, ma rallenta la crescita economica.
* **Industrial Policy:** aumenta del 25% sia l’economia sia l’inquinamento. Le zone industriali costano 500 €, mentre i parchi costano 200 €. Favorisce una crescita economica più rapida, al prezzo di un maggiore impatto ambientale.
I prezzi mostrati nell’interfaccia vengono aggiornati automaticamente quando viene selezionata una nuova politica.

### Eventi casuali
Durante la partita possono verificarsi eventi casuali. Può essere attivo un solo evento alla volta e il suo nome viene mostrato nel pannello laterale.

* **Attacco hacker:** una centrale elettrica scelta casualmente viene sospesa per 5 tick. Durante questo periodo gli edifici collegati rimangono associati alla centrale, ma sono considerati privi di energia. Al termine dell’evento la centrale riprende automaticamente a funzionare.
* **Crisi energetica:** per 5 tick viene sottratto dal budget un costo aggiuntivo di 50 € per ogni costruzione alimentata che consuma energia. Durante l’evento il budget viene evidenziato in rosso e gli edifici coinvolti sono segnalati sulla griglia.
* **Boom economico:** per 5 tick aumenta temporaneamente la produzione delle zone industriali e commerciali, comprese quelle costruite mentre l’evento è già in corso. Gli edifici potenziati sono contrassegnati da un simbolo verde.
* **Incendio:** il fuoco parte da una costruzione casuale diversa da una strada. A ogni tick può propagarsi agli edifici adiacenti in senso orizzontale o verticale, distruggendoli e riducendo la felicità. Le strade non possono bruciare e l’evento termina quando il fuoco non riesce più a propagarsi.
* **Tsunami:** proviene casualmente da nord, sud, est oppure ovest e coinvolge le prime quattro righe o colonne della mappa. Distrugge le costruzioni incontrate, con l’eccezione di strade e parchi, e riduce la felicità per 3 tick. La direzione di provenienza viene mostrata nel pannello dell’evento e l’avanzamento dell’onda è rappresentato graficamente sulla griglia.

### Salvataggio della partita
Il giocatore può salvare lo stato corrente attraverso il pulsante **Save Game**. Dalla schermata iniziale è possibile caricare il salvataggio e riprendere la simulazione mantenendo costruzioni, statistiche, budget, tick raggiunto, crescita degli edifici e politica attiva.

Sono inoltre disponibili i comandi per tornare alla schermata iniziale oppure riavviare la partita senza conservare i progressi non salvati.

## 2. Installazione e avvio

Per installare ed eseguire SimCity Lite è necessario:

* installare il **JDK 17**;
* installare **Apache Maven**;
* clonare il repository oppure scaricare ed estrarre il progetto;
* aprire un terminale nella cartella contenente il file `pom.xml`;
* eseguire il comando:

```bash
mvn clean javafx:run
```

Al primo avvio Maven scarica automaticamente da Maven Central le librerie necessarie. Successivamente viene avviata la classe `GUI.Main` e compare la schermata iniziale, dalla quale è possibile iniziare una nuova simulazione oppure caricarne una precedentemente salvata.
Il progetto può anche essere aperto con IntelliJ IDEA importando il file `pom.xml`, selezionando il **JDK 17** come Project SDK ed eseguendo l’obiettivo Maven `javafx:run`.
Per eseguire i test automatici si utilizza:

```bash
mvn test
```

## 3. Ambiente di esecuzione

Il progetto è sviluppato in **Java 17** e utilizza Maven per la compilazione e la gestione delle dipendenze. L’interfaccia grafica è realizzata con **JavaFX 17.0.20**.
Il software non richiede un database o un collegamento permanente a Internet. La connessione è necessaria solamente al primo avvio per consentire a Maven di scaricare le dipendenze. Il salvataggio della partita viene memorizzato localmente nel file `progress.json`, pertanto il programma deve avere il permesso di scrittura nella propria cartella di esecuzione.
Non sono presenti dipendenze specifiche da un sistema operativo: il progetto può essere eseguito su Windows, macOS e Linux, purché siano disponibili il JDK 17, Maven e un ambiente grafico.

## 4. Principali funzionalità riutilizzate da librerie esistenti

Il progetto utilizza le seguenti librerie:

* **JavaFX Controls 17.0.20:** utilizzata per realizzare l’interfaccia grafica attraverso finestre, scene, pulsanti, etichette, menu contestuali, messaggi di errore e contenitori per l’organizzazione degli elementi. Le classi `Application`, `Stage` e `Scene` gestiscono inoltre il ciclo di vita dell’applicazione.
* **JavaFX Charts:** le classi `LineChart`, `NumberAxis` e `XYChart` permettono di visualizzare l’andamento nel tempo di popolazione, economia, inquinamento, felicità e disoccupazione.
* **JavaFX Animation:** `Timeline`, `KeyFrame`, `KeyValue` e `PauseTransition` vengono utilizzate per le animazioni degli eventi e per la visualizzazione temporanea dei messaggi.
* **Gson 2.14.0:** converte lo stato della simulazione in formato JSON mediante `toJson()` e lo ricostruisce mediante `fromJson()`. `GsonBuilder` viene usato per produrre un file di salvataggio leggibile e indentato.
* **JUnit Jupiter 5.10.2:** utilizzata per organizzare ed eseguire i test automatici e per verificare i risultati mediante le principali funzioni di assertion.
* **Mockito 5.11.0:** utilizzata nei test per creare oggetti simulati e controllare il comportamento delle classi senza dover costruire tutte le dipendenze reali.

## 5. API esterne

Il software non utilizza API esterne e non comunica con servizi web, server remoti o database. Tutta la simulazione viene eseguita localmente. Maven Central viene utilizzato esclusivamente durante la configurazione del progetto per scaricare le librerie dichiarate nel file `pom.xml`.

## 6. Strumenti di intelligenza artificiale

Durante lo sviluppo sono stati utilizzati ChatGPT e Claude come strumento di supporto per l’analisi del codice, l’individuazione di possibili errori, la valutazione di alcune scelte progettuali, la scrittura e revisione dei test, dei commenti e della documentazione.
I suggerimenti prodotti dallo strumento sono stati controllati, adattati e verificati manualmente dai componenti del gruppo prima di essere inseriti nel progetto. Nessun sistema di intelligenza artificiale è integrato nel software o utilizzato durante l’esecuzione della simulazione.





