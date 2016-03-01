# compression
A set of compression tools for columnar data.

##Timestamp compression
Timestamp compression is based on Gorilla timestamp compression (http://www.vldb.org/pvldb/vol8/p1816-teller.pdf) with some modification to support larger delta values. Based on the use case at facebook, the paper makes some assumptions like two timestamps are never more than Integer.max seconds apart and only support seconds granularity. We've modified the aglorithm to handle some of those cases and also support timestamp at millisecond granularity. 

##Numeric compression
Numeric compression is also based on Gorilla paper.

###Bit packing
The library contains some helper writers/reader that allows one to easily pack/unpack bits into/from a byte array.
