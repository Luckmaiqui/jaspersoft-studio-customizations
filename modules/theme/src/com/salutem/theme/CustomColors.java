package com.salutem.theme;
import java.util.*;
import java.util.List;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

/**
 * User-chosen background colours for workbench blocks (editor area, views, stacks).
 * Works with any theme. Only structural containers are painted: report figures,
 * text, trees and tables keep their own colours. Removing a colour re-skins the
 * block so the active CSS theme restores it.
 */
public final class CustomColors {
 static final String NODE="com.salutem.jasper.theme.deepblue";
 static final String KEY="customColors";
 static final String EDITOR_AREA="org.eclipse.ui.editorss";
 /** Widget data key set by the E4 renderers (AbstractPartRenderer.OWNING_ME). */
 static final String OWNER="modelElement";
 private static final String PAINTED="salutem.customColor";
 private static boolean installed,queued;
 private static final Map<Integer,Color> POOL=new HashMap<>();

 /** One stored block: model element id, readable label and RGB colour. */
 public static final class Entry {
  public final String id,label;public final RGB rgb;
  public Entry(String id,String label,RGB rgb){this.id=id;this.label=label;this.rgb=rgb;}
 }

 private CustomColors(){}

 static boolean portuguese(){return Locale.getDefault().getLanguage().equals("pt");}
 static String text(String pt,String en){return portuguese()?pt:en;}

 static IEclipsePreferences preferences(){return InstanceScope.INSTANCE.getNode(NODE);}

 /** Format: id|label|RRGGBB, one entry per line. */
 public static List<Entry> load(){
  List<Entry> entries=new ArrayList<>();
  for(String line:preferences().get(KEY,"").split("\n")){
   String[] parts=line.split("\\|");
   if(parts.length!=3 || parts[0].isEmpty())continue;
   RGB rgb=parse(parts[2]);if(rgb!=null)entries.add(new Entry(parts[0],parts[1],rgb));
  }
  return entries;
 }
 public static void save(List<Entry> entries){
  StringBuilder value=new StringBuilder();
  for(Entry e:entries){if(value.length()>0)value.append('\n');value.append(e.id).append('|').append(clean(e.label)).append('|').append(hex(e.rgb));}
  IEclipsePreferences node=preferences();
  if(value.length()==0)node.remove(KEY);else node.put(KEY,value.toString());
  try{node.flush();}catch(org.osgi.service.prefs.BackingStoreException error){System.err.println("Salutem custom colours: "+error);}
 }
 public static void put(String id,String label,RGB rgb){
  List<Entry> entries=load();entries.removeIf(e->e.id.equals(id));entries.add(new Entry(id,label,rgb));save(entries);
 }
 static String clean(String label){return label==null?"":label.replace('|',' ').replace('\n',' ').trim();}
 public static String hex(RGB rgb){return String.format("%02X%02X%02X",rgb.red,rgb.green,rgb.blue);}
 public static RGB parse(String hex){
  String value=hex==null?"":hex.trim().replace("#","");
  if(!value.matches("[0-9a-fA-F]{6}"))return null;
  int v=Integer.parseInt(value,16);return new RGB((v>>16)&255,(v>>8)&255,v&255);
 }

 /** Opens the styled colour picker (hex code, eyedropper, recent colours). Returns null when cancelled. */
 static RGB chooseColor(Shell parent,RGB initial){
  return new ColorPickerDialog(parent,initial).open();
 }

 /** Called once from the theme start-up; independent of the active theme. */
 public static void install(Display display){
  if(installed || display.isDisposed())return;installed=true;
  // The CSS engine styles new or re-skinned widgets and stacks that change their
  // active/inactive class; repaint the chosen blocks right after it.
  for(int type:new int[]{SWT.Skin,SWT.Activate,SWT.FocusIn})display.addFilter(type,e->{if(hasEntries())schedule(display,80);});
  preferences().addPreferenceChangeListener(e->{if(KEY.equals(e.getKey()))display.asyncExec(()->schedule(display,0));});
  InstanceScope.INSTANCE.getNode("org.eclipse.e4.ui.css.swt.theme").addPreferenceChangeListener(e->display.asyncExec(()->schedule(display,600)));
  display.disposeExec(()->{for(Color c:POOL.values())if(!c.isDisposed())c.dispose();POOL.clear();});
  schedule(display,0);
  heartbeat(display);
 }
 private static void heartbeat(Display display){
  display.timerExec(2000,()->{if(display.isDisposed())return;if(hasEntries())applyAll(display);heartbeat(display);});
 }
 private static boolean hasEntries(){return !preferences().get(KEY,"").isEmpty();}
 static void schedule(Display display,int delay){
  if(queued || display.isDisposed())return;queued=true;
  display.timerExec(delay,()->{queued=false;if(!display.isDisposed())applyAll(display);});
 }

 private static Color color(Display display,RGB rgb){
  int key=(rgb.red<<16)|(rgb.green<<8)|rgb.blue;
  return POOL.computeIfAbsent(key,k->new Color(display,rgb));
 }

 static void applyAll(Display display){
  Map<String,RGB> wanted=new HashMap<>();for(Entry e:load())wanted.put(e.id,e.rgb);
  Set<Control> painted=Collections.newSetFromMap(new IdentityHashMap<>());
  for(Map.Entry<String,RGB> entry:wanted.entrySet())
   for(Control root:widgets(entry.getKey()))paint(root,color(display,entry.getValue()),root,painted);
  // Blocks whose colour was removed go back to the theme.
  for(Shell shell:display.getShells())restore(shell,painted);
 }

 /** Widgets of every model element with this id, in every workbench window. */
 static List<Control> widgets(String id){
  List<Control> controls=new ArrayList<>();
  try{
   MApplication application=PlatformUI.getWorkbench().getService(MApplication.class);
   EModelService models=PlatformUI.getWorkbench().getService(EModelService.class);
   if(application==null || models==null)return controls;
   for(MWindow window:application.getChildren())
    for(MUIElement element:models.findElements(window,id,MUIElement.class,null,EModelService.ANYWHERE))
     if(element.getWidget() instanceof Control && !((Control)element.getWidget()).isDisposed())controls.add((Control)element.getWidget());
  }catch(IllegalStateException workbenchClosing){ }
  return controls;
 }

 private static void paint(Control control,Color color,Control root,Set<Control> painted){
  if(control.isDisposed())return;
  // Never enter another part's content (e.g. the report designer inside the editor area).
  if(control!=root && control.getData(OWNER) instanceof MPart && !(root.getData(OWNER) instanceof MPart))return;
  if(structural(control)){
   if(!color.equals(control.getBackground()))control.setBackground(color);
   if(control instanceof CTabFolder){
    CTabFolder tabs=(CTabFolder)control;
    if(tabs.getItemCount()==0 && !color.equals(tabs.getSelectionBackground()))tabs.setSelectionBackground(color);
   }
   control.setData(PAINTED,Boolean.TRUE);painted.add(control);
  }
  if(control instanceof Composite)for(Control child:((Composite)control).getChildren())paint(child,color,root,painted);
 }
 private static void restore(Control control,Set<Control> painted){
  if(control.isDisposed())return;
  if(control.getData(PAINTED)!=null && !painted.contains(control)){
   control.setData(PAINTED,null);control.reskin(SWT.ALL);return;
  }
  if(control instanceof Composite)for(Control child:((Composite)control).getChildren())restore(child,painted);
 }

 /** Containers and plain labels only: content widgets keep their colours. */
 static boolean structural(Control c){
  Class<?> type=c.getClass();
  if(type==Composite.class || type==ScrolledComposite.class || type==SashForm.class)return true;
  if(c instanceof CTabFolder || c instanceof CLabel || (c instanceof Label && (c.getStyle()&SWT.SEPARATOR)==0))return true;
  if(c instanceof ToolBar)return true;
  String name=type.getName();
  return name.endsWith(".PageBook") || name.endsWith(".PageBook$1") || name.equals("org.eclipse.ui.part.PageBook");
 }

 /** The block under a control: the editor area as a whole, otherwise the nearest view or stack. */
 static MUIElement blockOf(Control control){
  MUIElement found=null;
  for(Control c=control;c!=null;c=c.getParent()){
   Object owner=c.getData(OWNER);
   if(owner instanceof MUIElement && ((MUIElement)owner).getElementId()!=null && !((MUIElement)owner).getElementId().isEmpty()){found=(MUIElement)owner;break;}
  }
  if(found==null)return null;
  MUIElement area=editorArea(found);
  return area!=null?area:found;
 }
 private static MUIElement editorArea(MUIElement element){
  for(MUIElement e=element;e!=null;e=e.getParent()!=null?e.getParent():e.getCurSharedRef())
   if(e instanceof MArea)return e;
  return null;
 }
 static String label(MUIElement element){
  if(EDITOR_AREA.equals(element.getElementId()) || element instanceof MArea)return text("Área do editor","Editor area");
  if(element instanceof MUILabel){String label=((MUILabel)element).getLocalizedLabel();if(label!=null && !label.isBlank())return label;}
  return element.getElementId();
 }
}
