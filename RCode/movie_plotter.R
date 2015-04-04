require(reshape)
require(Cairo)
require(ggplot2)
require(grid)
require(gridExtra)
#
args <- commandArgs(TRUE)
#
#
tsName=args[1]
#tsName="../data/asys40.txt"
tsData=read.csv(file=tsName,header=F,sep=",")
#
densityName=args[2]
#densityName="../movie/density1415283305267.csv"
densityData=read.csv(file=densityName,header=F,sep=",")
#
outFname=args[3]
#
df=data.frame(time=c(1:length(tsData$V1)),value=c(tsData$V1[0:length(densityData$V1)], rep(NA,length(tsData$V1)-length(densityData$V1))))
p <- ggplot(df, aes(time, value)) + geom_line(color="blue", lwd=0.3) + theme_bw() +
  ggtitle("Dataset ECG0606") +
theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())  
p  
#
pdf=data.frame(time=c(1:length(tsData$V1)),value=c(densityData$V1,rep(0,length(tsData$V1)-length(densityData$V1))))
shade <- rbind(c(0,0), pdf, c(length(densityData$V1),0))
names(shade)<-c("x","y")
p3 <- ggplot(pdf, aes(x=time,y=value)) +
  geom_line(col="cyan2") + theme_bw() + 
  geom_polygon(data = shade, aes(x, y), fill="cyan", alpha=0.5) +
  ggtitle("Sequitur rules density")+ 
  theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())
p3
print(arrangeGrob(p,p3), ncol=1)

pt <- Sys.time()

CairoJPEG(width = 1024, height = 768, 
      file=outFname,
      pointsize = 12, quality = 90,
      bg = "transparent", canvas = "white")
print(arrangeGrob(p,p3), ncol=1)
dev.off()