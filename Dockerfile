FROM ubuntu:latest
WORKDIR /app
COPY target/lgrcserver /app/lgrcserver
EXPOSE 8085
CMD ["/app/lgrcserver"]