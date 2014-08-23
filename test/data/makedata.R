## source the R code
#
source("../SAX/PAA_SAX.R")

## example data definitions
#
ts1.name <- "../data/timeseries01.csv"
ts2.name <- "../data/timeseries02.csv"

## load the data
#
ts1.data <- as.matrix(read.csv(file=ts1.name, header=FALSE, sep = ",", quote="\"", dec=".", fill = TRUE, comment.char=""))
ts2.data <- as.matrix(read.csv(file=ts2.name, header=FALSE, sep = ",", quote="\"", dec=".", fill = TRUE, comment.char=""))
ts1.data <- reshape(ts1.data, 1, nrow(ts1.data))
ts2.data <- reshape(ts2.data, 1, nrow(ts2.data))

## normalize series
#
ts1.norm <- znorm(ts1.data)
ts2.norm <- znorm(ts2.data)

## do PAA with reduction to 10 points
#
paaSize  <- 10
ts1.paa10 <- paa(ts1.norm, paaSize)
ts2.paa10 <- paa(ts2.norm, paaSize)

