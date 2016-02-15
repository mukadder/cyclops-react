package com.aol.cyclops.internal.stream.operators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.util.stream.StreamUtils;
import com.aol.cyclops.util.stream.Streamable;

import lombok.Value;
@Value
public class WindowWhileOperator<T> {
	 private static final Object UNSET = new Object();
	Stream<T> stream;
	public Stream<Streamable<T>> windowWhile(Predicate<? super T> predicate){
		Iterator<T> it = stream.iterator();
		return StreamUtils.stream(new Iterator<Streamable<T>>(){
			T value = (T)UNSET;
			@Override
			public boolean hasNext() {
				return value!=UNSET || it.hasNext();
			}
			@Override
			public Streamable<T> next() {
				
				List<T> list = new ArrayList<>();
				if(value!=UNSET)
					list.add(value);
				T value;
				while(list.size()==0 && it.hasNext()){
label:					while(it.hasNext()) {
							value=it.next();
							list.add(value);
							
							if(!predicate.test(value)){
								value=(T)UNSET;
								break label;
							}
							value=(T)UNSET;
						
					}
					
						
				}
				return Streamable.fromIterable(list);
			}
			
		});
	}
}