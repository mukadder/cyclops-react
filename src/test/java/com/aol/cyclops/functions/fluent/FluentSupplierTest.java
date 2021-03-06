package com.aol.cyclops.functions.fluent;

import static com.aol.cyclops.control.Matchable.otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.when;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FluentFunctions;
import com.aol.cyclops.control.Try;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class FluentSupplierTest {

	@Before
	public void setup(){
		this.times =0;
	}
	int called;
	public int getOne(){
		called++;
		return 1;
	}
	@Test
	public void testGet() {
		
		assertThat(FluentFunctions.of(this::getOne)
						.name("mySupplier")
						.println()
						.get(),equalTo(1));
		
	}
	@Test
	public void testCache() {
		called=0;
		Supplier<Integer> fn = FluentFunctions.of(this::getOne)
													  .name("myFunction")
													  .memoize();
		
		fn.get();
		fn.get();
		fn.get();
		
		assertThat(called,equalTo(1));
		
		
	}
	@Test
	public void testCacheGuava() {
		Cache<Object, Integer> cache = CacheBuilder.newBuilder()
			       .maximumSize(1000)
			       .expireAfterWrite(10, TimeUnit.MINUTES)
			       .build();

		called=0;
		Supplier<Integer> fn = FluentFunctions.of(this::getOne)
													  .name("myFunction")
													  .memoize((key,f)->cache.get(key,()->f.apply(key)));
		fn.get();
		fn.get();
		fn.get();
		
		assertThat(called,equalTo(1));
		
		
	}
	int set;
	public boolean events(){
		return set == 10;
		
	}
	@Test
	public void testBefore(){
		set = 0;
		assertTrue(FluentFunctions.of(this::events)
					   .before(()->set=10)
					   .println()
					   .get());
	}
	
	int in;
	boolean out;
	@Test
	public void testAfter(){
		set = 0;
		assertFalse(FluentFunctions.of(this::events)
					   .after(out->out=true)
					   .println()
					   .get());
		
		boolean result = FluentFunctions.of(this::events)
										.after((out2)->{ out=out2; } )
										.println()
										.get();
		
		
		assertTrue(out==result);
	}
	@Test
	public void testAround(){
		set = 0;
		assertThat(FluentFunctions.of(this::getOne)
					   .around(advice->advice.proceed())
					   .println()
					   .get(),equalTo(1));
		
		
	}
	
	int times =0;
	public String exceptionalFirstTime() throws IOException{
		if(times==0){
			times++;
			throw new IOException();
		}
		return   "hello world"; 
	}
	
	@Test
	public void retry(){
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
					   .println()
					   .retry(2,500)
					   .get(),equalTo("hello world"));
	}
	
	@Test
	public void recover(){
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
						.recover(IOException.class, ()->"hello boo!")
						.println()
						.get(),equalTo("hello boo!"));
	}
	@Test(expected=IOException.class)
	public void recoverDont(){
		assertThat(FluentFunctions.ofChecked(this::exceptionalFirstTime)
						.recover(RuntimeException.class, ()->"hello boo!")
						.println()
						.get(),equalTo("hello boo!"));
	}
	
	public String gen(String input){
		return input+System.currentTimeMillis();
	}
	@Test
	public void generate(){
		assertThat(FluentFunctions.of(this::gen)
						.println()
						.generate("next element")
						.onePer(1, TimeUnit.SECONDS)
						.limit(2)
						.toList().size(),equalTo(2));
	}
	
	
	@Test
	public void testMatches1(){
		assertThat(FluentFunctions.of(this::getOne)	
					   .matches(c->c.is(when(1),then(()->4)),otherwise(-1))
					   .get(),equalTo(4));
	}

	@Test
	public void testMatches1Default(){
		assertThat(FluentFunctions.of(this::getOne)	
					   .matches(c->c.is(when(4),then(()->4)),otherwise(-1))
					   .get(),equalTo(-1));
	}
	@Test
	public void testMatches2(){
		assertThat(FluentFunctions.of(this::getOne)	
					   .matches(c->c.is(when(1),then(()->5))
							   			.is(when(2),then(()->4)),otherwise(-1))
					   .get(),equalTo(5));
	}

	@Test
	public void testMatches2Default(){
		assertThat(FluentFunctions.of(this::getOne)	
					   .matches(c->c.is(when(100),then(()->5))
					   				.is(when(200),then(()->4)),otherwise(-1))
					   .get(),equalTo(-1));
	}
	
	
	@Test
	public void testLift(){
		
		FluentFunctions.of(this::getOne)	
						.lift()
						.get();
	}
	@Test
	public void testLiftM(){
		
		AnyM<Integer> result = FluentFunctions.of(this::getOne)	
											  .liftM()
											  .get();
		
		assertThat(result.stream().toList(),
					equalTo(Arrays.asList(1)));
	}
	@Test
	public void testTry(){
		
		Try<String,IOException> tried = FluentFunctions.ofChecked(this::exceptionalFirstTime)	
					   								   .liftTry(IOException.class)
					   								   .get();				  
		
		if(tried.isSuccess())
			fail("expecting failure");
		
	}
	Executor ex = Executors.newFixedThreadPool(1);
	@Test
	public void liftAsync(){
		assertThat(FluentFunctions.of(this::getOne)
						.liftAsync(ex)
						.get()
						.join(),equalTo(1));
	}
	@Test
	public void async(){
		assertThat(FluentFunctions.of(this::getOne)
						.async(ex)
						.thenApply(f->f.get())
						.join(),equalTo(1));
	}
	
	
}
