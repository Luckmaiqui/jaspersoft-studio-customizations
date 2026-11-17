package com.salutem.order;

import java.util.*;
import java.util.List;
import java.lang.reflect.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import com.jaspersoft.studio.model.ANode;
import com.jaspersoft.studio.repository.RepositoryView;

/** Local explorer filters. Server contents are searched only through the explicit native action. */
public final class ExplorerSearch {
 private static final String KEY="salutem.explorerSearch";
 private final TreeViewer viewer;
 private final boolean repository;
 private final Runnable remoteSearch;
 private final java.util.function.Supplier<IWorkspaceRoot> workspaceRoot;
 private Text text;private Label status;
 private String query="";private Set<String> visiblePaths=Set.of();
 private Object[] expandedBefore;private ISelection selectionBefore;
 private volatile int generation;private Job pending;
 private ExplorerSearch(TreeViewer viewer,boolean repository,Runnable remoteSearch,java.util.function.Supplier<IWorkspaceRoot> workspaceRoot){this.viewer=viewer;this.repository=repository;this.remoteSearch=remoteSearch;this.workspaceRoot=workspaceRoot;}
 public static void install(Display display){
  display.addFilter(SWT.KeyDown,e->{
   if((e.stateMask&SWT.MOD1)==0 || (e.stateMask&(SWT.ALT|SWT.SHIFT))!=0 || (e.keyCode!='f' && e.keyCode!='F'))return;
   if(!(e.widget instanceof Control))return;
   for(Control c=(Control)e.widget;c!=null;c=c.getParent())if(c.getData(KEY) instanceof ExplorerSearch){
    ExplorerSearch search=(ExplorerSearch)c.getData(KEY);e.doit=false;e.type=SWT.None;
    display.asyncExec(()->{if(!search.text.isDisposed()){if(search.repository && search.remoteSearch!=null)search.remoteSearch.run();else search.text.setFocus();}});return;
   }
  });
 }
 public static void attachOpenViews(){
  if(!PlatformUI.isWorkbenchRunning())return;
  for(IWorkbenchWindow window:PlatformUI.getWorkbench().getWorkbenchWindows())for(IWorkbenchPage page:window.getPages())for(IViewReference ref:page.getViewReferences()){
   IViewPart part=ref.getView(false);
   if(part instanceof RepositoryView){RepositoryView view=(RepositoryView)part;TreeViewer tree=view.getTreeViewer();attach(tree,true,()->openServerSearch(view,tree));}
   else if(part!=null && ref.getId().equals("org.eclipse.ui.navigator.ProjectExplorer"))try{
    Object tree=part.getClass().getMethod("getCommonViewer").invoke(part);
    if(tree instanceof TreeViewer)attach((TreeViewer)tree,false,null);
   }catch(ReflectiveOperationException error){System.err.println("Salutem project filter: "+error);}
  }
 }
 public static void attach(TreeViewer viewer,boolean repository,Runnable remoteSearch){
  attach(viewer,repository,remoteSearch,()->ResourcesPlugin.getWorkspace().getRoot());
 }
 static void attach(TreeViewer viewer,boolean repository,Runnable remoteSearch,java.util.function.Supplier<IWorkspaceRoot> workspaceRoot){
  if(viewer==null || viewer.getTree().isDisposed() || viewer.getTree().getData(KEY)!=null)return;
  new ExplorerSearch(viewer,repository,remoteSearch,workspaceRoot).create();
 }
 private void create(){
  Tree tree=viewer.getTree();Composite parent=tree.getParent();
  Composite wrapper=new Composite(parent,SWT.NONE);wrapper.setLayoutData(tree.getLayoutData());wrapper.moveAbove(tree);
  GridLayout layout=new GridLayout(1,false);layout.marginWidth=0;layout.marginHeight=0;layout.verticalSpacing=2;wrapper.setLayout(layout);
  if(!tree.setParent(wrapper)){wrapper.dispose();tree.setData(KEY,Boolean.FALSE);return;}
  Composite bar=new Composite(wrapper,SWT.NONE);bar.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));bar.moveAbove(tree);
  GridLayout row=new GridLayout(repository?2:1,false);row.marginWidth=2;row.marginHeight=2;bar.setLayout(row);
  text=new Text(bar,SWT.SEARCH|SWT.ICON_SEARCH|SWT.ICON_CANCEL|SWT.BORDER);text.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
  text.setMessage(pt()?(repository?"Pesquisar conexões por código ou nome…":"Pesquisar projetos, pastas e arquivos…"):(repository?"Search connections by code or name…":"Search projects, folders and files…"));
  text.setToolTipText(pt()?(repository?"Filtra bancos e conexões JasperServer. Ctrl+F: buscar no servidor.":"Pesquisa nomes e caminhos; não pesquisa o conteúdo dos arquivos."):"Search names and paths");
  if(repository){Button button=new Button(bar,SWT.PUSH);button.setText(pt()?"Buscar no servidor…":"Search server…");button.addListener(SWT.Selection,e->{if(remoteSearch!=null)remoteSearch.run();});button.setEnabled(remoteSearch!=null);}
  status=new Label(wrapper,SWT.NONE);status.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));status.moveAbove(tree);
  tree.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));tree.setData(KEY,this);wrapper.setData(KEY,this);
  bar.setBackground(tree.getBackground());wrapper.setBackground(tree.getBackground());text.setBackground(tree.getBackground());text.setForeground(tree.getForeground());status.setBackground(tree.getBackground());status.setForeground(tree.getForeground());
  viewer.addFilter(new ViewerFilter(){public boolean select(Viewer v,Object parent,Object element){
   if(query.isEmpty())return true;
   if(repository)return connectionMatches(element,query);
   IResource resource=resource(element);
   // JDT and working-set providers can wrap the workspace resources. Never
   // hide an unadaptable virtual parent, as that also hides all matching files.
   return resource==null || resource.getFullPath().segmentCount()==0 || visiblePaths.contains(resource.getFullPath().toPortableString()) || ConnectionSearch.matches(resource.getFullPath().toPortableString(),query);
  }});
  text.addModifyListener(e->changed());text.addListener(SWT.KeyDown,e->{if(e.keyCode==SWT.ESC){text.setText("");tree.setFocus();e.doit=false;}});
  tree.addListener(SWT.Dispose,e->{generation++;if(pending!=null)pending.cancel();});
  parent.layout(true,true);
 }
 static boolean connectionMatches(Object element,String query){
  for(Object node=element;node!=null;node=node instanceof ANode?((ANode)node).getParent():null){
   if(RepositoryOrder.kind(node)!=0)return ConnectionSearch.matches(((ANode)node).getDisplayText(),query);
  }
  return true; // Keep category headings; never traverse a server to find descendants.
 }
 static IResource resource(Object node){
  if(node instanceof IResource)return (IResource)node;
  if(node instanceof IAdaptable){IResource resource=((IAdaptable)node).getAdapter(IResource.class);if(resource!=null)return resource;}
  // IJavaProject/IJavaElement expose getResource(), but do not necessarily
  // adapt to IResource through getAdapter(IResource.class).
  if(node!=null)for(String method:new String[]{"getResource","getProject"})try{
   Method getter=node.getClass().getMethod(method);Object value=getter.invoke(node);if(value instanceof IResource)return (IResource)value;
  }catch(ReflectiveOperationException ignored){ }
  return null;
 }
 private void changed(){
  int token=++generation;if(pending!=null)pending.cancel();
  String requested=text.getText().strip();
  if(query.isEmpty() && !requested.isEmpty() && expandedBefore==null){expandedBefore=viewer.getExpandedElements();selectionBefore=viewer.getSelection();}
  if(requested.isEmpty()){
   query="";visiblePaths=Set.of();viewer.refresh(false);
   if(expandedBefore!=null){viewer.setExpandedElements(expandedBefore);viewer.setSelection(selectionBefore,false);expandedBefore=null;selectionBefore=null;}
   status.setText("");return;
  }
  status.setText(pt()?"Pesquisando…":"Searching…");
  text.getDisplay().timerExec(250,()->{if(!text.isDisposed() && generation==token){if(repository){query=requested;viewer.refresh(false);for(TreeItem item:viewer.getTree().getItems())if(group(item.getData()))viewer.setExpandedState(item.getData(),true);status.setText("");}else projectSearch(requested,token);}});
 }
 private static boolean group(Object node){for(Class<?> c=node==null?null:node.getClass();c!=null;c=c.getSuperclass())if(c.getName().equals("com.jaspersoft.studio.data.MDataAdapters") || c.getName().equals("com.jaspersoft.studio.server.model.server.MServers"))return true;return false;}
 private void projectSearch(String requested,int token){
  Display display=text.getDisplay();
  pending=new Job("JasperStudio: pesquisar projetos"){
   protected IStatus run(IProgressMonitor monitor){
    Set<String> paths=new HashSet<>();int[] count={0};
    IWorkspaceRoot root=workspaceRoot.get();
    try{
     root.accept((IResourceVisitor)r->{
      if(monitor.isCanceled() || generation!=token)return false;
      String path=r.getFullPath().toPortableString();
      if(ConnectionSearch.matches(path,requested)){count[0]++;for(IPath p=r.getFullPath();p.segmentCount()>0;p=p.removeLastSegments(1))paths.add(p.toPortableString());}
      return !(r instanceof IProject) || ((IProject)r).isOpen();
     });
    }catch(CoreException error){display.asyncExec(()->{if(!text.isDisposed() && generation==token)status.setText(pt()?"Não foi possível pesquisar todos os projetos":"Unable to search all projects");});return Status.CANCEL_STATUS;}
    if(monitor.isCanceled() || generation!=token)return Status.CANCEL_STATUS;
    Set<String> result=Set.copyOf(paths);display.asyncExec(()->{
     if(text.isDisposed() || generation!=token)return;
     visiblePaths=result;query=requested;viewer.refresh(false);
     // Expand only ancestor paths of matching local resources, with a cap for broad filters.
     if(result.size()<=500)expandMatches(viewer.getTree().getItems(),result,new int[]{500});
     status.setText(count[0]+(pt()?" itens encontrados":" items found"));
    });return Status.OK_STATUS;
   }
  };pending.setSystem(true);pending.schedule();
 }
 private void expandMatches(TreeItem[] items,Set<String> paths,int[] budget){
  for(TreeItem item:items){if(budget[0]--<=0)return;Object node=item.getData();IResource r=resource(node);
   if((r instanceof IContainer && paths.contains(r.getFullPath().toPortableString())) || node instanceof IWorkingSet){
    viewer.setExpandedState(node,true);if(!item.isDisposed())expandMatches(item.getItems(),paths,budget);
   }
  }
 }
 static List<Object> servers(TreeViewer viewer){
  List<Object> nodes=new ArrayList<>();
  if(!(viewer.getContentProvider() instanceof ITreeContentProvider))return nodes;
  ITreeContentProvider provider=(ITreeContentProvider)viewer.getContentProvider();
  for(Object group:provider.getElements(viewer.getInput())){
   if(RepositoryOrder.kind(group)==2)nodes.add(group);
   else if(group.getClass().getName().equals("com.jaspersoft.studio.server.model.server.MServers"))for(Object child:provider.getChildren(group))if(RepositoryOrder.kind(child)==2)nodes.add(child);
  }
  nodes.sort((a,b)->new NumericOrder().compare(((ANode)a).getDisplayText(),((ANode)b).getDisplayText()));return nodes;
 }
 static void selectServer(TreeViewer viewer,Object selected){
  Object filter=viewer.getTree().getData(KEY);
  if(filter instanceof ExplorerSearch)((ExplorerSearch)filter).text.setText("");
  viewer.setSelection(new StructuredSelection(selected),true);
 }
 private static void openServerSearch(RepositoryView view,TreeViewer viewer){
  List<Object> nodes=servers(viewer);
  if(nodes.isEmpty()){MessageDialog.openInformation(viewer.getTree().getShell(),"JasperStudio",pt()?"Nenhuma conexão JasperServer cadastrada.":"No JasperServer connections registered.");return;}
  ElementListSelectionDialog dialog=new ElementListSelectionDialog(viewer.getTree().getShell(),new LabelProvider(){public String getText(Object node){return ((ANode)node).getDisplayText();}});
  dialog.setTitle(pt()?"Pesquisar no JasperServer":"Search JasperServer");dialog.setMessage(pt()?"Escolha o servidor. A pesquisa será feita apenas nessa conexão.":"Choose the server to search.");dialog.setMultipleSelection(false);dialog.setElements(nodes.toArray());
  if(dialog.open()!=org.eclipse.jface.window.Window.OK)return;
  Object selected=dialog.getFirstResult();
  try{
   Field field=RepositoryView.class.getDeclaredField("rprovs");field.setAccessible(true);Object providers=field.get(view);
   if(providers instanceof Iterable)for(Object provider:(Iterable<?>)providers)if(provider.getClass().getName().equals("com.jaspersoft.studio.server.ServerProvider")){
    Class<?> job=Class.forName("com.jaspersoft.studio.server.wizard.find.FindResourceJob",true,provider.getClass().getClassLoader());
    Object result=NativeServerSearch.open(job,selected);
    if(result!=null){selectServer(viewer,selected);NativeServerSearch.reveal(job,provider,selected,result,viewer);}return;
   }
   throw new IllegalStateException("JasperServer search provider unavailable");
  }catch(ReflectiveOperationException|RuntimeException error){Throwable cause=NativeServerSearch.cause(error);System.err.println("JasperStudio server search: "+cause);MessageDialog.openError(viewer.getTree().getShell(),"JasperStudio",(pt()?"Não foi possível abrir a pesquisa do JasperServer.\n":"Unable to open JasperServer search.\n")+cause.getClass().getSimpleName()+": "+String.valueOf(cause.getMessage()));}
 }
 private static boolean pt(){return Locale.getDefault().getLanguage().equals("pt");}
}
