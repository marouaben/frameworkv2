package ets.genielog.appwatcher_3;
	public class Pair<A, B> {
		private A first;
		private B second;
		public Pair(A first, B second) {
			super();
			this.first = first;
			this.second = second;
		}

		public A getFirst(){return first;}
		public B getSecond(){return second;}
		public void SetFirst(A f){this.first=f;}
		public void SetSecond(B s){this.second=s;}
		
		
		@Override
		public String toString(){
			
			return first+";"+second;
			}
		


	    @Override	    
	    public int hashCode() {
	        return (first != null ? first.hashCode() : 0) + 31 * (second != null ? second.hashCode() : 0); //max = (a > b) ? a : b;
	     /**  return (res1+31*res2)
	      *  if (first != null) {
	        	  res1 = first.hashCode();
	        	}
	        	else {
	        	  res1 = 0;
	        	}
	        	
	        	 if (second != null) {
	        	  res2 = second.hashCode();
	        	}
	        	else {
	        	  res2 = 0;
	        	}
	        	
	        	**/
	      }

	    @Override
	      public boolean equals(Object o) {
	        if (o == null || o.getClass() != this.getClass()) { return false; }
	        Pair that = (Pair) o;
	        return (first == null ? that.first == null : first.equals(that.first))
	            && (second == null ? that.second == null : second.equals(that.second));
	      }

	}