import java.util.*;import java.util.List;import org.eclipse.swt.*;import org.eclipse.swt.widgets.*;import com.salutem.order.NumericOrder;
public class CheckOrder {
 static final class Adapter {String name;Adapter(String n){name=n;}}
 public static void main(String[]args){
  NumericOrder sorter=new NumericOrder();
  List<String> names=new ArrayList<>(List.of("(145) Hospital","(014) Unidade","(002) Unidade","CopyOf(001) Unidade","Sample DB","(001) Unidade","One Empty Record","(10) Unidade","(2) Unidade","CopyOf(017) Unidade","(017) Unidade"));names.sort(sorter);
  List<String> expected=List.of("One Empty Record","Sample DB","(001) Unidade","CopyOf(001) Unidade","(002) Unidade","(2) Unidade","(10) Unidade","(014) Unidade","(017) Unidade","CopyOf(017) Unidade","(145) Hospital");
  if(!names.equals(expected))throw new AssertionError(names);
  Map<String,Adapter> data=new LinkedHashMap<>();Adapter a=new Adapter("(145) Hospital"),b=new Adapter("(002) Unidade"),copy=new Adapter("(002) Unidade");data.put("idA",a);data.put("idB",b);data.put("copyB",copy);
  if(!sorter.sort(data,x->x.name))throw new AssertionError("Não ordenou");
  if(data.size()!=3||data.get("idA")!=a||data.get("idB")!=b||data.get("copyB")!=copy)throw new AssertionError("Perdeu identidade ou duplicata");
  if(sorter.sort(data,x->x.name))throw new AssertionError("Ordenação não é estável");
  Display display=new Display();Shell shell=new Shell(display);try{
   Menu menu=new Menu(shell,SWT.POP_UP);Adapter[] selected={null};
   for(Adapter adapter:data.values()){MenuItem item=new MenuItem(menu,SWT.PUSH);item.setText(adapter.name);item.setData("da.key",adapter);item.addListener(SWT.Selection,e->selected[0]=(Adapter)e.widget.getData("da.key"));}
   MenuItem last=menu.getItem(menu.getItemCount()-1);last.notifyListeners(SWT.Selection,new Event());if(selected[0]!=a)throw new AssertionError("Selecionou conexão errada");
  }finally{shell.dispose();display.dispose();}
  System.out.println("PASS: números, zeros à esquerda, cópias, nomes iguais, estabilidade e seleção do objeto correto.");
 }
}

