package com.salutem.order;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class GlobalSearchEngineTest {
 public static void main(String[] args)throws Exception{
  List<GlobalSearchEngine.Target> targets=new ArrayList<>();for(int i=0;i<8;i++)targets.add(new GlobalSearchEngine.Target(i,"Servidor "+i));
  AtomicInteger active=new AtomicInteger(),maximum=new AtomicInteger(),resultCount=new AtomicInteger(),errors=new AtomicInteger();CountDownLatch done=new CountDownLatch(8);
  GlobalSearchEngine.search(targets,"ficha padrão",(target,query,canceled)->{
   int n=active.incrementAndGet();maximum.accumulateAndGet(n,Math::max);
   try{Thread.sleep(35);if(target.connection().equals(3))throw new IllegalStateException("Indisponível");return List.of(new GlobalSearchEngine.Hit(target,query,"/relatorios/ficha","reportUnit",new Object()));}
   finally{active.decrementAndGet();}
  },update->{if(update.error()!=null)errors.incrementAndGet();else{if(!update.hits().get(0).target().equals(update.target()))throw new AssertionError("Origem perdida");resultCount.addAndGet(update.hits().size());}done.countDown();});
  if(!done.await(5,TimeUnit.SECONDS) || maximum.get()!=2 || resultCount.get()!=7 || errors.get()!=1)throw new AssertionError("Agregação, limite ou isolamento de erro");
  CountDownLatch started=new CountDownLatch(2),stopped=new CountDownLatch(2);AtomicInteger callbacks=new AtomicInteger(),calls=new AtomicInteger();
  var session=GlobalSearchEngine.search(targets,"cancelar",(target,query,canceled)->{calls.incrementAndGet();started.countDown();try{while(!canceled.getAsBoolean())Thread.sleep(5);return List.of();}finally{stopped.countDown();}},update->callbacks.incrementAndGet());
  if(!started.await(3,TimeUnit.SECONDS))throw new AssertionError("Não iniciou");session.cancel();if(!stopped.await(3,TimeUnit.SECONDS))throw new AssertionError("Não cancelou");Thread.sleep(100);
  if(callbacks.get()!=0 || calls.get()!=2)throw new AssertionError("Executou fila cancelada ou entregou resultado obsoleto");
  System.out.println("PASS: 8 servidores agregados; máximo 2 simultâneos; falha isolada; origem preservada; cancelamento de ativos e fila; nenhum resultado após cancelar.");
 }
}
