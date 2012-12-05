# Pac Defence v1.1.6

## Running the game

The command to run the game is:

    java -jar PacDefence.jar

Note that you need a Java virtual machine installed to run the game, version 6 or greater.

### Command line switches

*-d, --debugTimes*
> Display the times of different parts of the internals. Used for evaluating
performance.

*--debugPath*
> Draws some information about the path (that the creeps follow). This is primarily used when making a new map.

*-t N, --threads=N*
> Use N threads in the executor pool. If this isn't provided, or a value < 1 is given, it will use the number of processors that java detects. A value of 1 won't create an executor pool, instead using single-threaded code where the pool would normally be used.

## Help

Check out the manual, which is available on the web site, www.freewebs.com/pacdefence, or if you have the version of the jar that includes the source, it should be in there.

## Troubleshooting

If you find the game runs too slow at higher levels (particularly if you run with the --debugTimes command line switch and see the bullet time running high), try running the game with the server vm. In my (very) brief test, it was about twice as fast on level 30 and with ~4000 bullets with the server vm than the default client vm. Note that this will increase startup time. YMMV.

## Feedback

If you find any bugs or have any comments, you're more than welcome to email me at PacDefence[at]gmail[dot]com

(c) Liam Byrne 2008 - 2012

