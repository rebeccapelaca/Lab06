package it.polito.tdp.meteo;

import java.time.LocalDate;
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
		
		citta.add(Genova);
		citta.add(Torino);
		citta.add(Milano);
		
		int livello = 0;
		
		recursive(soluzione_parziale, soluzione_completa, livello, mese);
		
		String soluzione = "";
		
		for(SimpleCity sc : soluzione_completa)
			soluzione += sc.getNome() + "\n";
		
		return soluzione;	

	}
	
	public void recursive(List<SimpleCity> soluzione_parziale, List<SimpleCity> soluzione_completa, int livello, int mese) {
		
		int contatore_giorni = 1;
		int costo;
		
		if(contatore_giorni<=NUMERO_GIORNI_TOTALI) {
			
			if(punteggioSoluzione(soluzione_parziale)<punteggioSoluzione(soluzione_completa) && this.controllaParziale(soluzione_parziale)) {
				
				soluzione_completa.clear();
				soluzione_completa.addAll(soluzione_parziale);		
			}
		}
		
		for(Citta c : citta) {
			
			if(c.getCounter()<NUMERO_GIORNI_CITTA_MAX) {
				
				SimpleCity sc = new SimpleCity(c.getNome(),0);
				costo = calcolaCosto(soluzione_parziale, c.getRilevamenti(), contatore_giorni+1, mese);
				sc.setCosto(costo);
				soluzione_parziale.add(sc);
				c.increaseCounter();
				contatore_giorni += NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN;
				recursive(soluzione_parziale, soluzione_completa, livello+1, mese);
				soluzione_parziale.remove(sc);
				contatore_giorni -= NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN;	
			}
		}
	}	
		
	private int calcolaCosto(List<SimpleCity> soluzione_parziale, List<Rilevamento> rilevamenti, int giorno, int mese) {
		
		int costo = 0;
		int ultimo_inserito;
		LocalDate data_tmp = LocalDate.of(2013, mese , giorno);
		
		for(Rilevamento r : rilevamenti) {
			
			if(r.getData().equals(data_tmp)) {
				
				costo = r.getUmidita();
			
				ultimo_inserito = soluzione_parziale.size();
				
				if(!soluzione_parziale.get(ultimo_inserito-1).getNome().equals(soluzione_parziale.get(ultimo_inserito).getNome()))
					
					costo += COST;
			}
		}
		
		System.out.println(costo);
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
