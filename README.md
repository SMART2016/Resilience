# resilienceautomation

A Clojure library designed to ... well, that part is up to you.

## Usage

Fixme

## How do I access the Docker REST API remotely?


1. Edit the file /lib/systemd/system/docker.service

		sudo vi /lib/systemd/system/docker.service
2. Modify the line that starts with ExecStart to look like this:

		ExecStart=/usr/bin/docker daemon -H fd:// -H tcp://0.0.0.0:1111
		Save the modified file. Here I used port 1111, but any free port can be used.

3. Make sure the Docker service notices the modified configuration:

		systemctl daemon-reload
4. Restart the Docker service:
		
		sudo service docker restart
5. Test

		curl http://localhost:1111/version

6. See the result

		{"Version":"17.05.0-ce","ApiVersion":"1.29","MinAPIVersion":"1.12","GitCommit":"89658be","GoVersion":"go1.7.5","Os":"linux","Arch":"amd64","KernelVersion":"4.15.0-20-generic","BuildTime":"2017-05-04T22:10:54.638119411+00:00"}
		Now you can use the REST API.


## License

Copyright © 2019 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
