import org.eclipse.swt.widgets.*;import org.eclipse.swt.custom.*;import org.eclipse.swt.graphics.*;import org.eclipse.swt.SWT;import org.eclipse.draw2d.*;import org.eclipse.draw2d.text.TextFlow;import java.lang.reflect.*;
public class CheckUpdate {public static void main(String[]args)throws Exception{Display d=new Display();Shell shell=new Shell(d);try{
 com.salutem.l10n.Startup startup=new com.salutem.l10n.Startup();
 IFigure root=new Figure();TextFlow text=new TextFlow("Text Field");org.eclipse.draw2d.Label label=new org.eclipse.draw2d.Label("Page Number");org.eclipse.draw2d.text.FlowPage page=new org.eclipse.draw2d.text.FlowPage();page.add(text);root.add(page);root.add(label);
 Method translate=startup.getClass().getDeclaredMethod("translatePalette",IFigure.class);translate.setAccessible(true);translate.invoke(startup,root);
 if(!text.getText().equals("Campo de texto")||!label.getText().equals("Número da página"))throw new AssertionError("Paleta");
 CTabFolder folder=new CTabFolder(shell,SWT.NONE);Method tab=startup.getClass().getDeclaredMethod("tab",CTabItem.class);tab.setAccessible(true);
 for(String[] entry:new String[][]{{"Problems","Problemas"},{"Repository Explorer","Explorador de repositórios"}}){CTabItem item=new CTabItem(folder,SWT.NONE);item.setText(entry[0]);tab.invoke(startup,item);if(!item.getText().equals(entry[1]))throw new AssertionError("Aba: "+entry[0]);}
 Image flag=new Image(d,args[0]);if(flag.getBounds().width<10)throw new AssertionError("Bandeira inválida");flag.dispose();
 System.out.println("PASS: TextFlow e Label da paleta; abas Problemas e Explorador de repositórios; PNG da bandeira carregado.");
}finally{shell.dispose();d.dispose();}}}

