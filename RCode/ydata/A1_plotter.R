# libs: GRAPHICS
library(ggplot2)
library(grid)
library(gridExtra)
library(scales)
library(Cairo)
# libs: DATA
library(plyr)
library(dplyr)
library(stringr)
#
#

prefix <- "/media/Stock/git/grammarviz2_src.git/src/resources/sampler-IDEA/A1Benchmark/"
setwd(prefix)
ll = data.frame(base = list.files(path = ".", pattern = "*.csv$",recursive = F),stringsAsFactors = F)
ll$anomaly = paste(ll$base,".column.anomaly",sep = "")
head(ll)
#
plot_series1 <- function(dd,fname) {
  res <- ggplot(data = dd,aes(x = timestamp,y = value)) + theme_bw() + geom_line(colour = "darkgrey") +
    ggtitle(paste("YAHOO EGADS,",fname))
  is_in_anomaly <- 0
  start <- -1
  end <- -1
  for (i in c(1:length(dd$is_anomaly))) {
    v = dd$is_anomaly[i]
    if (v == 1 && is_in_anomaly == 0) {
      start <- i
      is_in_anomaly <- 1
    }
    if (v == 0 && is_in_anomaly == 1 || is_in_anomaly == 1 && i == length(dd$is_anomaly)) {
      end = i
      is_in_anomaly = 0
      red_line_segment = dd[(start:end),]
      res = res + geom_line(data = red_line_segment,aes(x = timestamp,y = value),color = "red",size = 1.5)
    }
  }
  res
}
#
plot_series2 <- function(dd,fname) {
  res = ggplot(data = dd,aes(x = timestamp,y = value)) + theme_bw() + geom_line(colour = "darkgrey") +
    ggtitle(paste("RRA,",fname))
  is_in_anomaly <- 0; start <- -1; end <- -1
  for (i in c(1:length(dd$rra_anomaly))) {
    v <- dd$rra_anomaly[i]
    if (v > 0 && is_in_anomaly == 0) {
      start <- i; is_in_anomaly <- 1
    }
    if (v == 0 && is_in_anomaly == 1 || is_in_anomaly == 1 && i == length(dd$rra_anomaly)) {
      end <- i; is_in_anomaly <- 0
      red_line_segment = dd[((start + 1):(end)),]
      red_line_segment$rank = as.character(red_line_segment$rra_anomaly[1] - 1)
      res = res + geom_line(data = red_line_segment,aes(x = timestamp,y = value,color = rank),size = 1.5)
    }
  }
  res + theme(legend.position = "bottom") + scale_colour_discrete(name = "Anomaly rank: ")
}
#
i <- 11
for(i in c(1:length(ll$base))){
  f <- ll[i,]
  print(paste(f$base))
  dat <- read.table(f$base,header = T,sep = ",")
  tmp <- read.table(f$anomaly,header = F,sep = ",")
  dat$rra_anomaly <- tmp$V1
  p1 <- plot_series1(dat, f$base)
  p2 <- plot_series2(dat, f$base)
  CairoPNG(file = paste(f$base,'.png',sep=""),width = 900, height = 600)
  print(grid.arrange(p1, p2, ncol=1))
  dev.off()
}