
all: 
	javac *.java

test:
	@mkdir test
	./test-100.sh 4 2 10

# simulation simple de 5 noeuds et 10 participants
simuler:
	./test-100.sh 5 10

clean:
	@rm *.class
	@rm -rf test
	@rm -rf log