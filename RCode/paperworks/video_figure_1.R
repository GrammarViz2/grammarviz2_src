require(reshape)
require(scales)
#
require(Cairo)
require(ggplot2)
require(RColorBrewer)
require(grid)
require(gridExtra)
require(lattice)
require(gtable)
#
#$ java -cp "grammarviz2-0.0.1-SNAPSHOT-jar-with-dependencies.jar" net.seninp.grammarviz.GrammarVizAnomaly -d anomaly_01.csv -alg RRAPRUNED -n 3 -w 160 -p 6 -a 6 -o out
#GrammarViz2 CLI anomaly discovery
#parameters:
  #  input file:                  anomaly_01.csv
#output files prefix:         out
#Algorithm implementation:    RRAPRUNED
#Num. of discords to report:  3
#SAX sliding window size:     160
#SAX PAA size:                6
#SAX alphabet size:           6
#SAX numerosity reduction:    EXACT
#SAX normalization threshold: 0.01
#GI Algorithm:                Sequitur
#
#16:51:23.318 [main] INFO  n.s.grammarviz.GrammarVizAnomaly - Reading data ...
#16:51:23.357 [main] INFO  n.s.grammarviz.GrammarVizAnomaly - read 3501 points from anomaly_01.csv
#16:51:23.358 [main] INFO  n.s.grammarviz.GrammarVizAnomaly - running RRA with pruning algorithm, building the grammar ...
#16:51:23.494 [main] INFO  n.s.grammarviz.GrammarVizAnomaly - 96 rules inferred in 85ms, pruning ...
#16:51:23.508 [main] INFO  n.s.grammarviz.GrammarVizAnomaly - finished pruning in 150ms, keeping 10 rules for anomaly discovery ...
#16:51:23.508 [main] INFO  n.s.grammarviz.GrammarVizAnomaly - found 3 intervals not covered by rules: [1099-1141],[1313-1420],[1780-1807],
#discord #0 "pos,calls,len,rule 1099 3592 42 -1", at 1099 distance to closest neighbor: 7.459850716707116, info string: "position 1099, length 42, NN distance 7.459850716707116, elapsed time: 11ms, distance calls: 3592"
#discord #1 "pos,calls,len,rule 1313 3246 107 -2", at 1313 distance to closest neighbor: 6.691643588097744, info string: "position 1313, length 107, NN distance 6.691643588097744, elapsed time: 9ms, distance calls: 3246"
#discord #2 "pos,calls,len,rule 1599 17647 181 43", at 1599 distance to closest neighbor: 3.2598292840118313, info string: "position 1599, length 181, NN distance 3.2598292840118313, elapsed time: 37ms, distance calls: 17647"
#Discords found in 210ms

#
data=read.csv(file="paperworks/runlogs/anomaly_01.csv",header=F,sep=",")
df=data.frame(time=c(1:length(data$V1)),value=data$V1)
p0 <- ggplot(df, aes(time, value)) + geom_line(lwd=0.6,color="grey10") + theme_classic() +
  ggtitle("Excerpt from the Video dataset") + 
  theme(plot.title = element_text(size = rel(1.8)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank(),axis.text.x=element_text(size = rel(1.8)))
p0  
#
#
p <- ggplot(df, aes(time, value)) + geom_line(lwd=0.6,color="grey10") + theme_classic() +
  ggtitle("Motifs and discords discovered in Video dataset") + 
  theme(plot.title = element_text(size = rel(1.8)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank(),axis.text.x=element_text(size = rel(1.8)))
p  
red_line=df[1099:(1099+42),]
p = p + geom_line(data=red_line,col="red", lwd=1.6)
green_line=df[1313:(1313+107),]
p = p + geom_line(data=green_line,col="red", lwd=1.6)
violet_line=df[1599:(1599+181),]
p = p + geom_line(data=violet_line,col="red", lwd=1.6)
p

motifs <- 
  "65,242
224,400
369,545
525,702
665,841
820,996
1569,1744
1869,2044
2029,2205
2177,2353
2320,2496
2465,2641
2614,2789
2768,2944
2918,3094
3069,3245
3226,3401"
dat = read.delim(textConnection(motifs),
                 header=FALSE,sep=",",strip.white=TRUE)
for(i in 1:length(dat$V1)){
  p = p + geom_line(data=df[dat[i,1]:dat[i,2],],col="blue", lwd=1.6)
}

#
density=read.csv(file="paperworks/runlogs/rra_out_coverage.txt",header=F,sep=",")
which(density==0)
density_df=data.frame(time=c(1:length(density$V1)),value=density$V1)
shade <- rbind(c(0,0), density_df, c(3502,0))
names(shade)<-c("x","y")
p1 <- ggplot(density_df, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_classic() + scale_y_continuous(limits = c(0, 36)) +
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Grammar rules density (132 rules)") + 
  theme(plot.title = element_text(size = rel(1.8)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.text.y=element_text(size = rel(1.8)),axis.text.x=element_text(size = rel(1.8)))
p1  
#
density2=read.csv(file="paperworks/runlogs/rrapruned_out_coverage.txt",header=F,sep=",")
which(density2==0)
density_df=data.frame(time=c(1:length(density2$V1)),value=density2$V1)
shade <- rbind(c(0,0), density_df, c(3502,0))
names(shade)<-c("x","y")
p2 <- ggplot(density_df, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_classic() + scale_y_continuous(limits = c(0, 8), breaks=c(0,2,4,6)) +
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Pruned grammar rules density (12 rules)") + 
  theme(plot.title = element_text(size = rel(1.8)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.text.y=element_text(size = rel(1.8)),axis.text.x=element_text(size = rel(1.8)))
p2  

rbind_gtable_max <- function(...){
  
  gtl <- list(...)
  stopifnot(all(sapply(gtl, is.gtable)))
  bind2 <- function (x, y) 
  {
    stopifnot(ncol(x) == ncol(y))
    if (nrow(x) == 0) 
      return(y)
    if (nrow(y) == 0) 
      return(x)
    y$layout$t <- y$layout$t + nrow(x)
    y$layout$b <- y$layout$b + nrow(x)
    x$layout <- rbind(x$layout, y$layout)
    x$heights <- gtable:::insert.unit(x$heights, y$heights)
    x$rownames <- c(x$rownames, y$rownames)
    x$widths <- grid::unit.pmax(x$widths, y$widths)
    x$grobs <- append(x$grobs, y$grobs)
    x
  }
  
  Reduce(bind2, gtl)
}

cbind_gtable_max <- function(...){
  
  gtl <- list(...)
  stopifnot(all(sapply(gtl, is.gtable)))
  bind2 <- function (x, y) 
  {
    stopifnot(nrow(x) == nrow(y))
    if (ncol(x) == 0) 
      return(y)
    if (ncol(y) == 0) 
      return(x)
    y$layout$l <- y$layout$l + ncol(x)
    y$layout$r <- y$layout$r + ncol(x)
    x$layout <- rbind(x$layout, y$layout)
    x$widths <- gtable:::insert.unit(x$widths, y$widths)
    x$colnames <- c(x$colnames, y$colnames)
    x$heights <- grid::unit.pmax(x$heights, y$heights)
    x$grobs <- append(x$grobs, y$grobs)
    x
  }
  Reduce(bind2, gtl)
}

grid.draw(rbind_gtable_max(ggplotGrob(p0), ggplotGrob(p1), ggplotGrob(p2), ggplotGrob(p)))

Cairo(width = 1000, height = 850, 
      file="video_excerpt.pdf", 
      type="pdf", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(grid.draw(rbind_gtable_max(ggplotGrob(p0), ggplotGrob(p1), ggplotGrob(p2), ggplotGrob(p))))
dev.off()
