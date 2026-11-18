/** Measures only repeated UI-label lookup, not total Studio/report execution time. */
public class TranslationBenchmark {
 public static void main(String[] args){
  com.salutem.l10n.Startup language=new com.salutem.l10n.Startup();
  String[] labels={"Banco de Dados","Nome do Relatório","Classe da Fábrica de Formatação","Campo de Texto","Propriedades","Fonte de Dados","Selecionar Servidores","Text Field","Report Name","Localizar na Árvore"};
  long checksum=0;for(int i=0;i<20000;i++)checksum+=language.translate(labels[i%labels.length]).length();
  long started=System.nanoTime();for(int i=0;i<200000;i++)checksum+=language.translate(labels[i%labels.length]).length();
  System.out.printf(java.util.Locale.ROOT,"200000 label lookups: %.2f ms; checksum=%d%n",(System.nanoTime()-started)/1000000d,checksum);
 }
}
