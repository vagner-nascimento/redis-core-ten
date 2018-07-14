# Redis Core Ten
A Java redis core implementation to use the ten most commons commands of Redis.

# Environment Variables to set:
- REDIS_SERVER (Required, the program doesn't run without that)
- REDIS_PORT (Optional, default: 6379)
- REDIS_PASSWORD (Optional)

# Run
- To run in console mode to enter with commands manually run "Console.java" file.
- To see a sample of every command just "CommandsTest.java" in tests folder.
- To run a stress and charge test with 4000 simultaneous threads sending SET, GET, DEL and INCR commands to the same registry run "SovietAttackTest.java".
- To run a atomicity test executing multi block and single commands run "AtomicityTest.java".

# Next steps
- Implement automatized unit and integration tests.
