FROM erlangsolutions/mongooseim:6.2.1

COPY docker/mongooseim.toml /usr/lib/mongooseim/etc/mongooseim.toml

EXPOSE 10000

CMD ["mongooseimctl", "foreground"]
