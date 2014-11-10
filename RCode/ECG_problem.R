require(ggplot2)
require(gridExtra)
require(plyr)
require(reshape)
require(Cairo)

# ECG 300
#
data=read.csv(file="../data/300_signal1.txt",header=F,sep=",", as.is=T)
plot(data$V1,t="l")

#
df=data.frame(time=c(1:length(data$V1)),value=data$V1)
p <- ggplot(df, aes(time, value)) + geom_line(lwd=1.1) + theme_classic() +
  ggtitle("Dataset ECG300") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank())
p  
#
# rules density
#
density=read.csv(file="../coverage.txt",header=F,sep=",",as.is=T)
density_df=data.frame(time=c(1:length(density$V1)),value=density$V1)
min(density)
which(density<20)
length(density[,1])
shade <- rbind(c(0,0), density_df, c(length(density[,1]),0))
names(shade)<-c("x","y")
pd <- ggplot(density_df[54800:55200,], aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_bw() +
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Sequitur rules density ") + 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank())
pd  

print(arrangeGrob(p,pd,ncol=2))


df=data.frame(time=c(1:length(data$V1)),value=data$V1)
p1 <- ggplot(df[54000:56000,], aes(time, value)) + geom_line(lwd=1.1,color="blue") + theme_classic() +
  ggtitle("Best HOTSAX discord") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank()) +
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(), axis.ticks.y=element_blank(),axis.text.y=element_blank())
red_line=df[54859:(54859+300),]
p1 = p1 + geom_line(data=red_line,col="red", lwd=1.6)
p1

p2 <- ggplot(df[441000:443000,], aes(time, value)) + geom_line(lwd=1.1,color="blue") + theme_classic() +
  ggtitle("Second HOTSAX discord") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank()) +
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(), axis.ticks.y=element_blank(),axis.text.y=element_blank())
red_line=df[441652:(441652+300),]
p2 = p2 + geom_line(data=red_line,col="red", lwd=1.6)
p2

p3 <- ggplot(df[236000:238000,], aes(time, value)) + geom_line(lwd=1.1,color="blue") + theme_classic() +
  ggtitle("Third HOTSAX discord") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank()) +
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(), axis.ticks.y=element_blank(),axis.text.y=element_blank())
red_line=df[236949:(236949+300),]
p3 = p3 + geom_line(data=red_line,col="red", lwd=1.6)
p3
#
#
p4 <- ggplot(df[54000:56000,], aes(time, value)) + geom_line(lwd=1.1,color="blue") + theme_classic() +
  ggtitle("Second RRA discord") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank()) +
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(), axis.ticks.y=element_blank(),axis.text.y=element_blank())
red_line=df[54796:(54796+312),]
p4 = p4 + geom_line(data=red_line,col="red", lwd=1.6)
p4

p5 <- ggplot(df[441000:443000,], aes(time, value)) + geom_line(lwd=1.1,color="blue") + theme_classic() +
  ggtitle("Third RRA discord") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank()) +
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(), axis.ticks.y=element_blank(),axis.text.y=element_blank())
red_line=df[441617:(441617+317),]
p5 = p5 + geom_line(data=red_line,col="red", lwd=1.6)
p5

p6 <- ggplot(df[200000:280000,], aes(time, value)) + geom_line(lwd=1.1,color="blue") + theme_classic() +
  ggtitle("Best RRA discord, ECG300") +
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank()) +
  theme(axis.title.x = element_blank(),axis.title.y=element_blank(), axis.ticks.y=element_blank(),axis.text.y=element_blank())
red_line=df[236947:(236947+302),]
p6 = p6 + geom_line(data=red_line,col="red", lwd=1.6)
p6


pd <- ggplot(data=density_df[200000:280000,], aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_bw() +
  ggtitle("Sequitur rules density, ECG300 ") + 
  theme(plot.title = element_text(size = rel(1.5)), 
        axis.title.x = element_blank(),axis.title.y=element_blank())
pd  
print(arrangeGrob(p6,pd, ncol=1))


print(arrangeGrob(p1,p6,p2,p4,p3,p5, ncol=2))

Cairo(width = 1000, height = 700, 
      file="ECG300.ps", 
      type="ps", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(arrangeGrob(p1,p6,p2,p4,p3,p5, ncol=2))
dev.off()
