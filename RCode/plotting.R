require(Cairo)
require(ggplot2)
require(grid)
require(gridExtra)
#
args <- commandArgs(TRUE)
datafname=args[1]
#"/home/psenin/git/hilbert/data/anomaly/anomaly_pruned_hilbert_curve_4Sequitur.csv"
coveragefname=args[2]
#"/home/psenin/git/hilbert/data/anomaly/coverage.txt"
distancefname=args[3]
#"/home/psenin/git/hilbert/data/anomaly/distances.txt"
print(paste("data file name",datafname))
print(paste("coverage",coveragefname))
print(paste("distance",distancefname))
#
data=read.csv(file=datafname,header=F,sep=",")
#
df=data.frame(time=c(1:length(data$V1)),value=data$V1)
p <- ggplot(df, aes(time, value)) + geom_line(color="blue", lwd=0.3) + theme_bw() +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank()) +  ggtitle(paste("Dataset",datafname))
#p  
#
density=read.csv(file=coveragefname,header=F,sep=",")
density_df=data.frame(time=c(1:length(density$V1)),value=density$V1)
shade <- rbind(c(0,0), density_df, c(length(data$V1),0))
names(shade)<-c("x","y")
p2 <- ggplot(density_df, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_bw() +
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Sequitur rules density for dataset") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
#p2
#
distances=read.csv(file=distancefname,header=F,sep=",")
df=data.frame(time=distances$V1,value=distances$V2,width=distances$V3)
p3 <- ggplot(df, aes(x=time, y=0)) + geom_segment(aes(xend=time, yend=value), color="red") + theme_bw() +
  ggtitle("Non-self distance to the nearest neighbor among subsequences corresponding to Sequitur rules") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
#p3 

#print(arrangeGrob(p,p2,p3), ncol=1)

png(width = 1000, height = 600,
    file="SAXSequitur.png", pointsize=12,
    bg = "transparent", canvas = "white", units = "px")
print(arrangeGrob(p,p2,p3), ncol=1)
dev.off()
