import java.util.*;
import com.salutem.order.NumericOrder;
public final class NumericOrderTest {
 public static void main(String[] args) {
  NumericOrder comparator=new NumericOrder();
  List<String> values=new ArrayList<>(List.of("(100) Unidade","CopyOf(001) Unidade","(014) Unidade","(002) Unidade","Sample DB","(001) Unidade","One Empty Record","(10) Unidade"));
  values.sort(comparator);
  if(!values.equals(List.of("One Empty Record","Sample DB","(001) Unidade","CopyOf(001) Unidade","(002) Unidade","(10) Unidade","(014) Unidade","(100) Unidade")))throw new AssertionError(values);
  for(String a:values)for(String b:values)if(Integer.signum(comparator.compare(a,b))!=-Integer.signum(comparator.compare(b,a)))throw new AssertionError("Antissimetria");
  Object first=new Object(),second=new Object();Map<String,Object> data=new LinkedHashMap<>();data.put("(10)",first);data.put("(2)",second);
  IdentityHashMap<Object,String> names=new IdentityHashMap<>();names.put(first,"(10)");names.put(second,"(2)");
  if(!comparator.sort(data,names::get)||data.get("(10)")!=first||data.get("(2)")!=second||data.size()!=2)throw new AssertionError("Identidade da conexão");
  if(comparator.sort(data,names::get))throw new AssertionError("Idempotência");
  System.out.println("PASS: ordenação numérica, cópias, comparador e identidade das conexões.");
 }
}
