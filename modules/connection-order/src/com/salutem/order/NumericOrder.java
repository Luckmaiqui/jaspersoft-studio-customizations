package com.salutem.order;
import java.util.*;import java.util.function.Function;import java.util.regex.*;import java.math.BigInteger;import java.text.Collator;
/** Total ordering: non-numbered utility choices, then numeric IDs, original labels as tie breakers. */
public final class NumericOrder implements Comparator<String> {
 private static final Pattern ID=Pattern.compile("\\((\\d+)\\)|^\\s*(\\d+)");
 private final Collator collator=Collator.getInstance(new Locale("pt","BR"));
 private BigInteger id(String name){Matcher matcher=ID.matcher(name);return matcher.find()?new BigInteger(matcher.group(1)!=null?matcher.group(1):matcher.group(2)):null;}
 public int compare(String left,String right){if(Objects.equals(left,right))return 0;if(left==null)return -1;if(right==null)return 1;BigInteger a=id(left),b=id(right);int result;
  if(a==null&&b!=null)return -1;if(a!=null&&b==null)return 1;
  if(a!=null){result=a.compareTo(b);if(result!=0)return result;boolean copyA=!left.stripLeading().startsWith("("),copyB=!right.stripLeading().startsWith("(");if(copyA!=copyB)return copyA?1:-1;}
  result=collator.compare(left,right);return result!=0?result:left.compareTo(right);
 }
 public <K,V> boolean sort(Map<K,V> source,Function<V,String> label){
  List<Map.Entry<K,V>> entries=new ArrayList<>(source.entrySet());
  List<Map.Entry<K,V>> sorted=new ArrayList<>(entries);
  sorted.sort((a,b)->{int c=compare(label.apply(a.getValue()),label.apply(b.getValue()));return c!=0?c:compare(String.valueOf(a.getKey()),String.valueOf(b.getKey()));});
  boolean changed=false;for(int i=0;i<entries.size();i++)if(!Objects.equals(entries.get(i).getKey(),sorted.get(i).getKey())){changed=true;break;}
  if(changed){source.clear();for(Map.Entry<K,V> entry:sorted)source.put(entry.getKey(),entry.getValue());}
  return changed;
 }
}
