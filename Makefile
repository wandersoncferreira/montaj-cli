all: clean uberjar native deploy

uberjar:
	@echo "Building jar file"
	@lein uberjar

native:
	@echo "Building native source code"
	@lein native

deploy:
	@echo "Moving to local/bin"
	@cp $(CURDIR)/target/montag $(HOME)/.local/bin/montag

clean:
	@echo "Cleaning up..."
	@lein do clean
