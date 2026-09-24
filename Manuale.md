# SimCity Lite — Manuale di gioco

## Obiettivo del gioco

SimCity Lite è un piccolo simulatore gestionale in cui bisogna far crescere una città mantenendo sotto controllo **budget, popolazione, economia, inquinamento, felicità, energia e posti di lavoro**.

La città è costruita su una griglia **20 × 20**. Ogni pressione di **Next Turn** fa avanzare la simulazione di un turno e aggiorna edifici, statistiche, eventi e budget.

La partita inizia con **2500 €**.

## Costruzione della città

La prima strada può essere costruita liberamente. Tutte le strade successive devono essere adiacenti a una strada già esistente.

Gli altri edifici devono invece essere costruiti accanto a una strada. Alcune costruzioni diventano disponibili solo più avanti nella partita oppure richiedono particolari condizioni, come un numero sufficiente di lavoratori.

Costi base principali:

| Costruzione | Costo base |
| --- | ---: |
| Strada | 50 € |
| Zona residenziale | 200 € |
| Zona commerciale | 300 € |
| Zona industriale | 600 € |
| Parco | 150 € |
| Centrale elettrica | 800 € |
| Banca | 800 € |
| Impresa edile | 1000 € |
| Stazione di polizia | 1200 € |
| Base militare | 2250 € |
| Impianto di trattamento rifiuti | 1600 € |
| Centrale nucleare | 3000 € |

Le politiche cittadine possono modificare alcuni prezzi.

Le strade non possono essere demolite. Per demolire le altre costruzioni serve almeno un'**impresa edile alimentata**; il numero di demolizioni disponibili dipende dalle imprese edili presenti e si rinnova periodicamente.

## Popolazione, lavoro ed economia

Le zone residenziali ospitano gli abitanti della città. Se sono alimentate, la popolazione cresce; se rimangono senza corrente, la popolazione diminuisce e la zona può infine scomparire.

Le zone commerciali e industriali offrono posti di lavoro e producono denaro. Per costruirle deve essere disponibile un numero sufficiente di cittadini senza lavoro.

Il valore **Economy** indica quanto la città sta producendo economicamente in quel momento. Non rappresenta il denaro già disponibile: quello è il **Budget**. L'economia deriva soprattutto dalla produzione delle zone commerciali e industriali alimentate, può essere modificata dalla policy attiva e può essere ridotta da effetti negativi come le attività criminali.

La produzione dei singoli edifici economici cresce gradualmente nel tempo finché rimangono alimentati. Le industrie possono produrre molto più dei commerciali, ma richiedono più lavoratori, consumano più energia, generano più inquinamento e hanno costi di manutenzione maggiori.

A ogni turno il gioco usa quindi, in modo semplificato, questa relazione:

**variazione del Budget = Economy - costi di manutenzione**

A questo risultato possono poi aggiungersi altri costi o effetti, per esempio eventi, assicurazioni o rimborso dei prestiti. Se un edificio commerciale o industriale non è alimentato, non produce economia.

## Energia

Le centrali elettriche alimentano le costruzioni vicine entro la propria area di copertura.

Ogni edificio consuma una quantità diversa di energia. Se una centrale non riesce ad alimentare tutto, alcune costruzioni vengono scollegate; quando torna disponibile energia sufficiente, vengono ricollegate automaticamente.

La rete cittadina ha inoltre una capacità massima complessiva di **10000**. Se una nuova costruzione supererebbe questo limite, non può essere piazzata finché la rete non viene estesa.

Quando la rete si avvicina al limite compare la **centrale nucleare** tra le costruzioni disponibili. Una centrale nucleare costa **3000 €** e aumenta la capacità massima della rete di **10000**. La centrale nucleare può essere costruita anche quando la rete è già al limite, proprio perché serve a estenderla.

La centrale nucleare ha però effetti negativi su felicità e inquinamento. Se viene rimossa o distrutta, provoca un'esplosione che può danneggiare le costruzioni vicine; le strade non vengono distrutte dall'esplosione.

Gli edifici senza energia sono indicati nella griglia con il simbolo del fulmine.

## Manutenzione

Oltre al costo iniziale di costruzione, alcune strutture hanno un **costo di manutenzione per turno**, che viene sottratto dal guadagno della città.

Per esempio:
- strada: 2 € per turno;
- zona residenziale: 5 €;
- parco: 10 €;
- zona commerciale: 30 €;
- banca: 60 €;
- impresa edile: 40 €;
- zona industriale: 200 €;
- stazione di polizia: 120 €;
- impianto di trattamento rifiuti: 160 €.

Per questo motivo non basta costruire molto: bisogna assicurarsi che l'economia riesca a sostenere i costi della città.

Se la città rimane per molti turni in una situazione economica critica, può andare incontro al fallimento.

## Felicità e inquinamento

Le costruzioni influenzano in modo diverso la città.

I parchi aumentano la felicità e riducono l'inquinamento. Le zone commerciali aumentano moderatamente entrambi, mentre le industrie producono molto inquinamento e riducono la felicità.

L'**impianto di trattamento rifiuti** è una struttura costosa da mantenere, ma permette di ridurre in modo significativo l'inquinamento della città.

La generazione automatica dell'erba è attualmente disabilitata.

## Politiche cittadine

La città può utilizzare tre politiche:

- **Standard Policy**: comportamento normale;
- **Environmental Policy**: favorisce la riduzione dell'inquinamento, ma riduce anche l'economia;
- **Industrial Policy**: favorisce l'economia, aumentando però anche l'inquinamento.

La politica non può essere cambiata continuamente: dopo un cambio devono passare **12 turni** prima di poterla modificare di nuovo.

## Banche e prestiti

Le banche diventano disponibili più avanti nella partita.

Una banca alimentata permette di chiedere un prestito. Più banche alimentate sono presenti, maggiore può essere l'importo massimo richiesto.

Il prestito diventa da restituire dopo 3 turni. L'importo da restituire è pari a **1,5 volte** la somma richiesta, quindi va usato con attenzione.

## Protezione avanzata delle centrali nucleari

Se sono presenti almeno una **banca** e una **centrale nucleare non protetta**, il gioco propone automaticamente di applicare una protezione avanzata contro gli incendi.

Il costo base è di **2000 € per centrale nucleare**. Ogni banca applica uno **sconto del 5%**, fino a un massimo del **30%**. Una centrale nucleare protetta non può essere distrutta da un incendio.

## Assicurazione contro lo tsunami

Se è presente almeno una banca, è possibile acquistare un'**assicurazione contro lo tsunami**.

Il prezzo dipende dalle costruzioni presenti nelle zone della mappa che possono essere raggiunte dallo tsunami e dal loro tipo. Ogni banca applica uno **sconto del 5%** sul costo complessivo, fino a uno sconto massimo del **30%**.

L'assicurazione copre soltanto gli edifici presenti nel momento in cui viene applicata. Se in seguito vengono costruiti nuovi edifici nella zona a rischio, è necessario premere nuovamente il pulsante dell'assicurazione per estendere la copertura soltanto a quelle nuove costruzioni.

Quando uno tsunami distrugge edifici assicurati, questi vengono ricostruiti gradualmente dopo la fine dell'animazione, mantenendo lo stato che avevano prima della distruzione. Durante la ricostruzione non è possibile costruire nella zona interessata.

## Basi militari

Le **basi militari** diventano disponibili dal **turno 80**. Consumano energia e aumentano l'inquinamento. Una base alimentata può eliminare un gruppo terroristico attivo da almeno **2 turni**; dopo un intervento deve attendere prima di agire di nuovo. La presenza di almeno una base permette inoltre di acquistare la difesa missilistica.

## Difesa missilistica

Nelle fasi avanzate della partita, se hai almeno una **base militare**, puoi acquistare il sistema di difesa missilistica. Il costo base è di **20000 €**: ogni base militare applica uno **sconto del 5% sull'acquisto**, fino al **30%**.

Il sistema intercetta fino a **3 missili**. Dopo un'intercettazione puoi ripristinare un utilizzo alla volta oppure premere **Restore to 3** per riportarlo subito alla capacità massima. Ogni utilizzo ripristinato costa **300 €**, senza sconti; se il budget non basta per il ripristino completo, non viene effettuato alcun addebito.

## Loggia massonica

Al turno **500** ricevi un invito alla loggia massonica. Se accetti, una **Masonic Lodge** gialla con l'occhio nel triangolo appare automaticamente non appena c'è una casella costruibile disponibile; la scelta resta nel salvataggio.

## Attività criminali, terrorismo e polizia

Durante la partita possono comparire delle **attività criminali**, che riducono economia e felicità.

I **gruppi terroristici** causano vittime tra gli abitanti e riducono la popolazione finché restano attivi. Possono essere eliminati dalla polizia soltanto dopo essere rimasti attivi per almeno **5 turni**.

Una stazione di polizia alimentata può eliminare una minaccia alla volta, scegliendo tra attività criminali e gruppi terroristici. Le attività criminali devono essere rimaste attive per almeno **3 turni**. Dopo un intervento la stazione deve attendere prima di poter agire di nuovo.

## Eventi casuali

Durante la partita può essere attivo un solo evento casuale alla volta.

Gli eventi principali sono:

- **Attacco hacker**: sospende temporaneamente una centrale elettrica;
- **Crisi energetica**: aumenta temporaneamente i costi legati agli edifici alimentati e può portare il budget sotto zero;
- **Boom economico**: aumenta temporaneamente la produzione di zone commerciali e industriali;
- **Incendio**: può propagarsi tra costruzioni vicine e distruggerle;
- **Tsunami**: arriva da uno dei quattro lati della mappa e colpisce una parte della città;
- **Attacco missilistico**: nelle fasi avanzate della partita può colpire aree casuali della città e distruggere costruzioni.

Lo tsunami distrugge anche i parchi. Le strade non vengono distrutte né dallo tsunami né dagli attacchi missilistici. Le costruzioni assicurate possono essere ricostruite dopo lo tsunami.

Alcuni eventi possono inoltre causare una diminuzione temporanea della felicità.

## Salvataggio

La partita può essere salvata con **Save Game** e caricata successivamente dalla schermata iniziale.

Il salvataggio conserva le principali informazioni della città, comprese le costruzioni, il budget, il turno raggiunto, la politica, lo stato degli edifici e gli eventuali prestiti o debiti attivi.

## Avvio del gioco

Sono necessari **JDK 17** e **Apache Maven**.

Dalla cartella che contiene il file `pom.xml`:

```bash
mvn clean javafx:run
```

Per eseguire i test:

```bash
mvn test
```
