require(ggplot2)
require(grid)
require(gridExtra)
#
prefix="../movie/"
#
files=list.files(path = prefix,pattern="*.csv")
files = data.frame(num=as.numeric(gsub(".csv","",gsub("density","",files))),name=as.character(files))
files=files[order(files[,1]),]
#
datasetName = "ASYS 40"
tsName="../data/../data/asys40.txt"
tsData=read.csv(file=tsName,header=F,sep=",")
#
movieFrameCounter=0;
idx=seq(1,length(files[,2]),by=1)
for(i in idx){
 print(paste(i,": ",files[i,2],sep=""))
 densityName=paste(prefix,as.character(files[i,2]),sep="")
 densityData=read.csv(file=densityName,header=F,sep=",")
 #
 outFname=paste(prefix,"frame",sprintf("%04d", movieFrameCounter),".jpg",sep="")
 #
 df=data.frame(time=c(1:length(tsData$V1)),value=c(tsData$V1[0:length(densityData$V1)], rep(NA,length(tsData$V1)-length(densityData$V1))))
 p <- ggplot(df, aes(time, value)) + geom_line(color="blue", lwd=0.3) + theme_bw() +
   ggtitle(paste("Dataset",datasetName)) +
 theme(plot.title = element_text(size = rel(1.5)), axis.title.x = element_blank(),axis.title.y=element_blank(),
        axis.ticks.y=element_blank(),axis.text.y=element_blank())  
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

 g=arrangeGrob(p,p3,ncol=1)

 #ggsave(filename = outFname, 
 #       plot = g, 
 #        height=10.67,width=14.22,dpi=72)
 
 CairoJPEG(width = 1280, height = 720, 
      file=outFname,
       pointsize = 12, quality = 90,
       bg = "transparent", canvas = "white")
 print(arrangeGrob(p,p3), ncol=1)
  dev.off()
 
 movieFrameCounter=movieFrameCounter+1
}

# ffmpeg -r 30 -sameq -aq 3 -i frame%04d.jpg test1800.mp4
# vlc test1800.mp4
