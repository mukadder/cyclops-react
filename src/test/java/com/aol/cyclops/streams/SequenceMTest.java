package com.aol.cyclops.streams;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.Semigroups;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public class SequenceMTest {
	
    @Test
    public void combine(){
        assertThat(ReactiveSeq.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX(),equalTo(ListX.of(4,3))); 
                   
    }
	@Test
	public void subStream(){
		List<Integer> list = ReactiveSeq.of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(ReactiveSeq.of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(ReactiveSeq.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(ReactiveSeq.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(ReactiveSeq.of(ReactiveSeq.of(1, 2, 3),
        		ReactiveSeq.of(1, 3, 2), ReactiveSeq.of(2, 1, 3), ReactiveSeq.of(2, 3, 1), ReactiveSeq.of(3, 1, 2), ReactiveSeq.of(3, 2, 1)).map(s->s.toList()).toList()));
    }
    
    @Test
    public void emptyAllCombinations() {
        assertThat(ReactiveSeq.of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(ReactiveSeq.of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }

  

    @Test
    public void emptyCombinations() {
        assertThat(ReactiveSeq.of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(ReactiveSeq.of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(ReactiveSeq.of()
							.onEmptySwitch(()->ReactiveSeq.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));
				
	}
	@Test
	public void onEmptySwitch(){
		assertThat(ReactiveSeq.of(4,5,6)
							.onEmptySwitch(()->ReactiveSeq.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));
				
	}
	
	@Test
	public void elapsedIsPositive(){
		
		
		assertTrue(ReactiveSeq.of(1,2,3,4,5).elapsed().noneMatch(t->t.v2<0));
	}
	@Test
	public void timeStamp(){
		
		
		assertTrue(ReactiveSeq.of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t.v2 <= System.currentTimeMillis()));
		

	}
	@Test
	public void elementAt0(){
		assertThat(ReactiveSeq.of(1).elementAt(0).v1,equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(ReactiveSeq.of(1,2,3,4,5).elementAt(2).v1,equalTo(3));
	}
	@Test
	public void getMultpleStream(){
		assertThat(ReactiveSeq.of(1,2,3,4,5).elementAt(2).v2.toList(),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test(expected=NoSuchElementException.class)
	public void getMultiple1(){
		ReactiveSeq.of(1).elementAt(1);
	}
	@Test(expected=NoSuchElementException.class)
	public void getEmpty(){
		ReactiveSeq.of().elementAt(0);
	}
	@Test
	public void get0(){
		assertTrue(ReactiveSeq.of(1).get(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(ReactiveSeq.of(1,2,3,4,5).get(2).get(),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(ReactiveSeq.of(1).get(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(ReactiveSeq.of().get(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(ReactiveSeq.of(1).single(),equalTo(1));
	}
	@Test(expected=UnsupportedOperationException.class)
	public void singleEmpty(){
		ReactiveSeq.of().single();
	}
	@Test(expected=UnsupportedOperationException.class)
	public void single2(){
		ReactiveSeq.of(1,2).single();
	}
	@Test
	public void singleOptionalTest(){
		assertThat(ReactiveSeq.of(1).singleOptional().get(),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(ReactiveSeq.of().singleOptional().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(ReactiveSeq.of(1,2).singleOptional().isPresent());
	}
	@Test
	public void limitTime(){
		List<Integer> result = ReactiveSeq.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4)));
	}
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
		List<Integer> result = ReactiveSeq.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void skipTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	private int sleep(Integer i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {
			
		}
		return i;
	}
	@Test
	public void testSkipLast(){
		assertThat(ReactiveSeq.of(1,2,3,4,5)
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(ReactiveSeq.of()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(ReactiveSeq.of(1,2,3,4,5)
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(ReactiveSeq.of()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(ReactiveSeq.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(ReactiveSeq.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(ReactiveSeq.of()
				.endsWithIterable(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(ReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(ReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Stream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Stream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(ReactiveSeq.of()
				.endsWith(Stream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWith(Stream.of()));
	}
	@Test
	public void anyMTest(){
		List<Integer> list = ReactiveSeq.of(1,2,3,4,5,6)
								.anyM().filter(i->i>3).stream().toList();
		
		assertThat(list,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void streamable(){
		Streamable<Integer> repeat = ReactiveSeq.of(1,2,3,4,5,6)
												.map(i->i*2)
												.toStreamable();
		
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	
	@Test
	public void concurrentLazyStreamable(){
		Streamable<Integer> repeat = ReactiveSeq.of(1,2,3,4,5,6)
												.map(i->i*2)
												.toConcurrentLazyStreamable();
		
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	@Test
	public void splitBy(){
		assertThat( ReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v1.toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( ReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v2.toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = ReactiveSeq.of(1,2,3,4,5)
											.peek(System.out::println)
											.toLazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = ReactiveSeq.of(1,2,3,4,5)
											.peek(System.out::println)
											.toConcurrentLazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	int peek = 0;
	@Test
	public void testPeek() {
		peek = 0 ;
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(i-> peek=i)
				  				.collect(Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void headAndTailTest(){
		Stream<String> s = Stream.of("hello","world");
		Iterator<String> it = s.iterator();
		String head = it.next();
		Stream<String> tail = StreamUtils.stream(it);
		tail.forEach(System.out::println);
	}
	@Test
	public void testOfType() {

		

		assertThat(ReactiveSeq.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(ReactiveSeq.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(ReactiveSeq.of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}

	@Test
	public void testCastPast() {
		ReactiveSeq.of(1, "a", 2, "b", 3, null).cast(Date.class).map(d -> d.getTime());
	



	}
	
	@Test
	public void flatMapCompletableFuture(){
		assertThat(ReactiveSeq.of(1,2,3).flatMapAnyM(i->AnyM.fromCompletableFuture(CompletableFuture.completedFuture(i+2)))
				  								.collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapMaybe(){
		assertThat(ReactiveSeq.of(1,2,3,null).flatMapIterable(Maybe::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testIntersperse() {
		
		assertThat(ReactiveSeq.of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}
	@Test(expected=ClassCastException.class)
	public void cast(){
		ReactiveSeq.of(1,2,3).cast(String.class).collect(Collectors.toList());
	}
	@Test
	public void xMatch(){
		assertTrue(ReactiveSeq.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}
	
	
}
