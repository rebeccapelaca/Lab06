package it.polito.tdp.meteo;

import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.meteo.bean.Citta;
import it.polito.tdp.meteo.bean.Rilevamento;
import it.polito.tdp.meteo.bean.SimpleCity;
import it.polito.tdp.meteo.db.MeteoDAO;

public class Model {

	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	
	private List<SimpleCity> soluzione_completa;
	private List<SimpleCity> soluzione_parziale;
	private List<Citta> citta;
	
	MeteoDAO meteodao;

	public Model() {
		
		meteodao = new MeteoDAO();
	}

	public String getUmiditaMedia(int mese) {
			
		String umidita_media = "";
		
		umidita_media += "Genova: " + meteodao.getAvgRilevamentiLocalitaMese(mese, "Genova") + "\n";
		umidita_media += "Milano: " + meteodao.getAvgRilevamentiLocalitaMese(mese, "Milano") + "\n";
		umidita_media += "Torino: " + meteodao.getAvgRilevamentiLocalitaMese(mese, "Torino") + "\n";

		return umidita_media;	
	}

	public String trovaSequenza(int mese) {
		
		soluzione_completa = new LinkedList<SimpleCity>();
		soluzione_parziale = new LinkedList<SimpleCity>();
		citta = new LinkedList<Citta>();
		
		Citta Genova = new Citta("Genova", meteodao.getAllRilevamentiLocalitaMese(mese, "genova"));
		Citta Torino = new Citta("Torino", meteodao.getAllRilevamentiLocalitaMese(mese, "torino"));
		Citta Milano = new Citta("Milano", meteodao.getAllRilevamentiLocalitaMese(mese, "milano"));
		//Genova.stampa();
		//System.out.println(Genova.getRilevamenti());
		
		citta.add(Genova);
		citta.add(Torino);
		citta.add(Milano);
		
		int livello = 0;
		
		recursive(soluzione_parziale, soluzione_completa, livello);
		
		String soluzione = "";
		
		for(SimpleCity sc : soluzione_completa)
			soluzione += sc.getNome() + "\n";
		
		return soluzione;	

	}
	
	public void recursive(List<SimpleCity> soluzione_parziale, List<SimpleCity> soluzione_completa, int livello) {
		
		int contatore_giorni = 1;
		int costo;
		
		//System.out.println(soluzione_completa);
		
		if(contatore_giorni>=NUMERO_GIORNI_TOTALI) {
			
			if(punteggioSoluzione(soluzione_parziale)<punteggioSoluzione(soluzione_completa) && this.controllaParziale(soluzione_parziale)) {
				
				soluzione_completa.clear();
				soluzione_completa.addAll(soluzione_parziale);
				System.out.println(soluzione_completa);
			}
		}
		
		for(Citta c : citta) {
			
			if(c.getCounter()<NUMERO_GIORNI_CITTA_MAX) {
				
				//System.out.println(c.getCounter());
				
				SimpleCity sc = new SimpleCity(c.getNome(),0);		
				costo = calcolaCosto(soluzione_parziale, c.getRilevamenti(), contatore_giorni+1);
				sc.setCosto(costo);
				soluzione_parziale.add(sc);
				c.increaseCounter();
				contatore_giorni += NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN;
				System.out.println(contatore_giorni);
				recursive(soluzione_parziale, soluzione_completa, livello+1);
				soluzione_parziale.remove(sc);
				//contatore_giorni -= NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN;	
			}
		}
	}	
		
	private int calcolaCosto(List<SimpleCity> soluzione_parziale, List<Rilevamento> rilevamenti, int giorno) {
		
		int costo = 0;
		int ultimo_inserito;
		
		Rilevamento r = null;
		
		//System.out.println(rilevamenti);
		//System.out.println(giorno);
		
		if(rilevamenti.size()>=giorno-1)
			r = rilevamenti.get(giorno-1);
		
		//System.out.println(r);
				
		costo = r.getUmidita();
			
		ultimo_inserito = soluzione_parziale.size();
		
		if(ultimo_inserito>1)
			if(!soluzione_parziale.get(ultimo_inserito-2).getNome().equals(soluzione_parziale.get(ultimo_inserito-1).getNome()))
				costo += COST;	
		
		//System.out.println(soluzione_parziale);
		//System.out.println(costo);
		
		return costo;
	}

	private int punteggioSoluzione(List<SimpleCity> soluzioneCandidata) {
		
		int costo_totale = 0;
		
		for(SimpleCity sc : soluzioneCandidata) 
			costo_totale += sc.getCosto();
		
		return costo_totale;
	}

	private boolean controllaParziale(List<SimpleCity> parziale) {
		
		for(Citta c : citta) 
			if(c.getCounter()<NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN)
				return false;		
		return true;
	}
}
