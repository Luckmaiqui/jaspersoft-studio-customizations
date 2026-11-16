package com.salutem.l10n;
import java.util.*;import java.io.*;import org.eclipse.ui.IStartup;import org.eclipse.swt.widgets.*;import org.eclipse.swt.custom.*;import org.eclipse.draw2d.FigureCanvas;import org.eclipse.draw2d.IFigure;
/** Complements localized resource fragments. Never changes editable text, combo values or document models. */
public final class Startup implements IStartup {
 private final Properties dictionary=new Properties();
 public Startup(){try(InputStream stream=getClass().getResourceAsStream("/translations.properties")){if(stream!=null)dictionary.load(stream);}catch(IOException error){System.err.println("Salutem pt_BR: "+error);}}
 public String translate(String original){
  if(original==null || original.isEmpty())return original;
  int tab=original.indexOf('\t');String suffix=tab>=0?original.substring(tab):"";String text=tab>=0?original.substring(0,tab):original;
  String translated=dictionary.getProperty(text);
  if(translated==null)translated=dictionary.getProperty(text.replace("&",""));
  if(translated==null && text.endsWith(":")){String bare=text.substring(0,text.length()-1);String found=dictionary.getProperty(bare);if(found!=null)translated=found+":";}
  if(translated==null && text.endsWith("...")){String bare=text.substring(0,text.length()-3);String found=dictionary.getProperty(bare);if(found!=null)translated=found+"...";}
  if(translated==null && text.matches("Page \\d+ of \\d+"))translated=text.replaceFirst("Page (\\d+) of (\\d+)","Página $1 de $2");
  if(translated==null && text.matches("Save ['\"].*['\"]\\?"))translated="Salvar"+text.substring(4);
  if(translated==null)return original;
  if(text.contains("&") && !translated.contains("&"))translated="&"+translated;
  return translated+suffix;
 }
 public String translateButton(String original){
  String translated=translate(original);
  if(translated!=null && translated.matches("(?i)&?(?:Salvar|Save)\\s*[@\\t]\\s*(?:Ctrl|Control)\\s*\\+\\s*S"))return "Salvar";
  return translated;
 }
 public void earlyStartup(){if(!Locale.getDefault().getLanguage().equals("pt"))return;Display d=Display.getDefault();d.asyncExec(new Runnable(){public void run(){if(d.isDisposed())return;try{for(Shell s:d.getShells())visit(s);}finally{if(!d.isDisposed())d.timerExec(1200,this);}}});}
 private void menu(Menu m){if(m==null||m.isDisposed())return;for(MenuItem i:m.getItems()){String t=translate(i.getText());if(!t.equals(i.getText()))i.setText(t);menu(i.getMenu());}}
 private void visit(Control c){if(c.isDisposed())return;try{
  if(c instanceof Shell){Shell s=(Shell)c;String t=translate(s.getText());if(!t.equals(s.getText()))s.setText(t);menu(s.getMenuBar());}
  if(c instanceof Label){Label l=(Label)c;String t=translate(l.getText());if(!t.equals(l.getText()))l.setText(t);}
  if(c instanceof CLabel){CLabel l=(CLabel)c;String t=translate(l.getText());if(!t.equals(l.getText()))l.setText(t);}
  if(c instanceof Button){Button b=(Button)c;String t=translateButton(b.getText());if(!t.equals(b.getText()))b.setText(t);}
  if(c instanceof Group){Group g=(Group)c;String t=translate(g.getText());if(!t.equals(g.getText()))g.setText(t);}
  if(c instanceof ToolBar)for(ToolItem i:((ToolBar)c).getItems()){String t=translate(i.getText());if(!t.equals(i.getText()))i.setText(t);String tip=translate(i.getToolTipText());if(tip!=null&&!tip.equals(i.getToolTipText()))i.setToolTipText(tip);}
  if(c instanceof Table)for(TableColumn i:((Table)c).getColumns()){String t=translate(i.getText());if(!t.equals(i.getText()))i.setText(t);}
  if(c instanceof Tree)for(TreeItem item:((Tree)c).getItems()) {
   if(Set.of("Data Adapters","Servers").contains(item.getText()))item.setText(translate(item.getText()));
  }
  if(c instanceof Tree)for(TreeColumn i:((Tree)c).getColumns()){String t=translate(i.getText());if(!t.equals(i.getText()))i.setText(t);}
  if(c instanceof CTabFolder)for(CTabItem i:((CTabFolder)c).getItems())tab(i);
  if(c instanceof TabFolder)for(TabItem i:((TabFolder)c).getItems()){String t=translate(i.getText());if(!t.equals(i.getText()))i.setText(t);}
  if(c instanceof FigureCanvas)figures(((FigureCanvas)c).getContents(),0);
  menu(c.getMenu());String tip=translate(c.getToolTipText());if(tip!=null&&!tip.equals(c.getToolTipText()))c.setToolTipText(tip);
 }catch(RuntimeException error){if(c.getData("salutem.ptBR.error")==null){c.setData("salutem.ptBR.error",true);System.err.println("Salutem pt_BR: "+error);}}
  if(c instanceof Composite)for(Control child:((Composite)c).getChildren())visit(child);
 }
 private void tab(CTabItem item){String text=item.getText();if(Set.of("Design","Source","Preview","Main Report","Report","Advanced","Problems","Repository Explorer","Project Explorer","Report State","Console","Errors","Statistics","Fields","Parameters","Sorting","Data preview").contains(text)){String t=translate(text);if(!t.equals(text))item.setText(t);}}
 private void figures(IFigure figure,int depth){if(figure==null||depth>25)return;
  // Only palette figures; report figures can contain user-authored content.
  boolean palette=figure.getClass().getName().contains("palette");
  if(palette)translatePalette(figure);else for(Object child:figure.getChildren())figures((IFigure)child,depth+1);
 }
 private void translatePalette(IFigure f){
 if(f instanceof org.eclipse.draw2d.text.TextFlow){
  org.eclipse.draw2d.text.TextFlow flow=(org.eclipse.draw2d.text.TextFlow)f;
  String translated=translate(flow.getText());if(!translated.equals(flow.getText()))flow.setText(translated);
 }
 if(f instanceof org.eclipse.draw2d.Label){org.eclipse.draw2d.Label l=(org.eclipse.draw2d.Label)f;String t=translate(l.getText());if(!t.equals(l.getText()))l.setText(t);}for(Object child:f.getChildren())translatePalette((IFigure)child);}
}


