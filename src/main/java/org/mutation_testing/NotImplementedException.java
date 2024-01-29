package org.mutation_testing;

public class NotImplementedException extends RuntimeException {
    
        private static final long serialVersionUID = 1L;
    
        public NotImplementedException() {
            super("Not implemented");
        }
    
        public NotImplementedException(String message) {
            super(message);
        }

}
