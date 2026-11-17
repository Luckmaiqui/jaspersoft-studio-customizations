import java.util.*;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import com.jaspersoft.studio.data.*;
import com.jaspersoft.studio.data.noda.NoDataAdapterDescriptor;
import com.jaspersoft.studio.server.model.server.*;
import com.jaspersoft.studio.utils.jasper.JasperReportsConfiguration;
import com.salutem.order.RepositoryOrder;

public class CheckRepositoryOrder {
 static MDataAdapter adapter(String name){return new MDataAdapter(null,new NoDataAdapterDescriptor(){@Override public String getTitle(){return name;}});}
 static MServerProfile server(String name){ServerProfile profile=new ServerProfile();profile.setName(name);return new MServerProfile(null,profile){@Override public JasperReportsConfiguration getJasperConfiguration(){return null;}};}
 public static void main(String[]args){
  Display d=new Display();Shell shell=new Shell(d);
  try {
   Object a=adapter("(145) Hospital"),b=adapter("(002) Hospital"),copy=adapter("CopyOf(002) Hospital");
   Object sa=server("(103) Servidor"),sb=server("(014) Servidor"),same=server("(014) Servidor");
   String root="root",adapters="Adaptadores",servers="Servidores",folder="Pasta remota";
   Map<Object,List<Object>> children=new IdentityHashMap<>();
   children.put(root,List.of(adapters,servers,folder));children.put(adapters,new ArrayList<>(List.of(a,copy,b)));children.put(servers,new ArrayList<>(List.of(sa,sb,same)));
   children.put(folder,List.of("Z relatório","A relatório"));
   TreeViewer viewer=new TreeViewer(shell,SWT.NONE);
   viewer.setContentProvider(new ITreeContentProvider(){
    public Object[] getElements(Object input){return getChildren(input);}
    public Object[] getChildren(Object parent){return children.getOrDefault(parent,List.of()).toArray();}
    public boolean hasChildren(Object node){return children.containsKey(node);}
    public Object getParent(Object node){for(var e:children.entrySet())if(e.getValue().contains(node))return e.getKey();return null;}
   });
   viewer.setLabelProvider(new LabelProvider(){public String getText(Object n){return n instanceof com.jaspersoft.studio.model.ANode?((com.jaspersoft.studio.model.ANode)n).getDisplayText():String.valueOf(n);}});
   viewer.setInput(root);viewer.expandToLevel(2);viewer.setSelection(new StructuredSelection(sa));
   RepositoryOrder.attach(viewer);
   TreeItem[] categories=viewer.getTree().getItems();
   if(categories[0].getData()!=adapters || categories[1].getData()!=servers || categories[2].getData()!=folder)throw new AssertionError("Reordenou categorias");
   if(categories[0].getItem(0).getData()!=b || categories[0].getItem(1).getData()!=copy || categories[0].getItem(2).getData()!=a)throw new AssertionError("Adaptadores não ordenados");
   if(categories[1].getItem(0).getData()!=sb || categories[1].getItem(1).getData()!=same || categories[1].getItem(2).getData()!=sa)throw new AssertionError("Servidores/duplicatas");
   if(!categories[0].getExpanded() || viewer.getStructuredSelection().getFirstElement()!=sa)throw new AssertionError("Perdeu expansão/seleção");
   if(!categories[2].getItem(0).getText().equals("Z relatório"))throw new AssertionError("Reordenou relatórios remotos");
   if(children.get(adapters).get(0)!=a || children.get(servers).get(0)!=sa)throw new AssertionError("Alterou modelo persistido");
   ((MServerProfile)sa).getValue().setName("(001) Renomeado");viewer.update(sa,new String[]{"name"});viewer.refresh(servers);
   if(categories[1].getItem(0).getData()!=sa)throw new AssertionError("Renomeação não ordenou");
   Object comparator=viewer.getComparator();RepositoryOrder.attach(viewer);if(viewer.getComparator()!=comparator)throw new AssertionError("Reinstalou comparador");
   Object[] mixed={a,"fixo",b};((RepositoryOrder)comparator).sort(viewer,mixed);if(mixed[0]!=b || !mixed[1].equals("fixo") || mixed[2]!=a)throw new AssertionError("Lista mista");
   System.out.println("PASS: adaptadores e JasperServer reais; ordem numérica; cópias/duplicatas; seleção; expansão; renomeação; categorias e relatórios preservados.");
  } finally {shell.dispose();d.dispose();}
 }
}
