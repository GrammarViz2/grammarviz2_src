library(plyr)
library(dplyr)
library(stringr)
#
#
prefix <- "/media/Stock/git/grammarviz2_src.git/src/resources/sampler-IDEA/A2Benchmark/"
setwd(prefix)
#
ll <- data.frame(base = list.files(path = ".", pattern = "*.out$",recursive = F),stringsAsFactors = F)
#
head(ll)
#
ll$cmd <- aaply(ll$base, 1, function(x) {
  dat <- read.table(x, as.is = T, sep = ",", header = T)
  dat$rule_reduction <- dat$prunedRules/dat$grammarRules
  dat <- filter(dat, coverage >= 0.98)
  dd <- arrange(dat, rule_reduction)[1,]
  #
  cmd <- paste(
    "java -cp ", shQuote("grammarviz2-0.0.1-SNAPSHOT-jar-with-dependencies.jar", type = "csh"),
    " net.seninp.tinker.SamplerAnomaly",
    " -d ", prefix, gsub(".out", "", x),
    " -o ", prefix, gsub(".out", "", x), ".anomaly",
    " -w ", dd$window,
    " -a ", dd$alphabet,
    " -p ", dd$paa,
    " -n 5", sep = "")
  return(cmd)
})  
#
write.table(ll$cmd, "run.sh", row.names = F, col.names = F,  quote = F)
