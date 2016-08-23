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
#chfdb_chf01_275.txt
data=read.csv(file="../data/ecg0606_1.csv",header=F,sep=",")
plot(data$V1,t="l")
#
df=data.frame(time=c(1:length(data$V1)),value=data$V1)
p <- ggplot(df, aes(time, value)) + geom_line(lwd=1.1,color="blue1") + theme_bw() +
  ggtitle("Dataset ECG qtdb 0606 [701-3000]") + 
  theme(plot.title = element_text(size = rel(1.2)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p  
red_line=df[356:(356+105),]
p = p + geom_line(data=red_line,col="red", lwd=1.6)
p
#
density=read.csv(file="data/ecg0606_coverage.txt",header=F,sep=",")
density_df=data.frame(time=c(1:length(density$V1)),value=density$V1)
density_df$value[400:600]=density_df$value[400:600]-1
shade <- rbind(c(0,0), density_df, c(2229,0))
names(shade)<-c("x","y")
p1 <- ggplot(density_df, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_bw() +
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Sequitur rules density for (WIN=100, PAA=9, ALPHABET=5)") + 
  theme(plot.title = element_text(size = rel(1.2)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p1  
#
distances=read.csv(file="data/ecg0606_distances.txt",header=F,sep=",")
df=data.frame(time=c(1:length(data$V1)),value=distances$V2,width=distances$V3)
p2 <- ggplot(df, aes(time, value)) + geom_line(color="red") + theme_bw() +
  ggtitle("Non-self distance to the nearest neighbor among rule-corresponding subsequences") + 
  theme(plot.title = element_text(size = rel(0.98)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p2  

grid.arrange(p,p1,p2, layout_matrix = rbind(c(1), c(2), c(3)))

Cairo(width = 600, height = 450, 
      file="fig_05_ecg0606_RRA.pdf", 
      type="pdf", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(grid.arrange(p,p1,p2, layout_matrix = rbind(c(1), c(2), c(3))))
dev.off()
