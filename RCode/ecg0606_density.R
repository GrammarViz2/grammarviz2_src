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
# plot the data
#
data=read.csv(file="../test/data/ecg0606_1.csv",header=F,sep=",", as.is=T)
plot(data$V1,t="l")
#
df=data.frame(time=c(1:length(data$V1)),value=data$V1)
p <- ggplot(df, aes(time, value)) + geom_line(lwd=1.1) + theme_classic() +
 ggtitle("Dataset ECG0606") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank())
p  
red_line=df[386:(386+195),]
p = p + geom_line(data=red_line,col="red", lwd=1.6)
p

#
# rules density
#
density=read.csv(file="../density_curve.txt",header=F,sep=",",as.is=T)
density_df=data.frame(time=c(1:length(density$V1)),value=density$V1)
length(density[,1])
shade <- rbind(c(0,0), density_df, c(length(density[,1]),0))
names(shade)<-c("x","y")
p1 <- ggplot(density_df, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_bw() +
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Sequitur rules density ") + 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank())
p1  

print(arrangeGrob(p,p1, ncol=1))
