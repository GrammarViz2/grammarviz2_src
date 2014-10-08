require(reshape)
require(scales)
#
require(Cairo)
require(ggplot2)
require(RColorBrewer)
require(grid)
require(gridExtra)
require(lattice)
#
data=read.csv(file="../data/anomaly_pruned_hilbert_curve_4Sequitur.csv",header=F,sep=",")
df=data.frame(time=c(1:length(data$V1)),value=data$V1)
p <- ggplot(df, aes(time, value)) + geom_line(lwd=1.1,color="blue1") + theme_classic() +
  ggtitle("GPS Track") + 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p  
red_line=df[4659:(4659+366),]
p = p + geom_line(data=red_line,col="red", lwd=1.6)
green_line=df[6574:(6574+375),]
p = p + geom_line(data=green_line,col="green", lwd=1.6)
p
#
density=read.csv(file="../coverage.txt",header=F,sep=",")
which(density==0)
density_df=data.frame(time=c(1:length(density$V1)),value=density$V1)
density_df$value[400:600]=density_df$value[400:600]-1
shade <- rbind(c(0,0), density_df, c(2229,0))
names(shade)<-c("x","y")
p1 <- ggplot(density_df, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_classic() +
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Sequitur rules density for (w=350,p=15,a=4)") + 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p1  
#
distances=read.csv(file="../distances.txt",header=F,sep=",")
df=data.frame(time=c(1:length(data$V1)),value=distances$V2,width=distances$V3)
p2 <- ggplot(df, aes(time, value)) + geom_line(color="red") + theme_classic() +
  ggtitle("Non-self distance to the nearest neighbor among subsequences corresponding to Sequitur rules") + 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p2  

print(arrangeGrob(p,p1,p2,ncol=1))

Cairo(width = 1000, height = 600, 
      file="GPS.pdf", 
      type="pdf", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(arrangeGrob(p,p1,p2, ncol=1))
dev.off()

