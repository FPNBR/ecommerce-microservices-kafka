import os
import threading

threads = []
build_failed = False

def build_application(app):
    global build_failed
    threads.append(app)
    print("Building application {}".format(app))
    exit_code = os.system("cd {} && mvn clean package -DskipTests".format(app))
    if exit_code != 0:
        print("Build failed for application {}".format(app))
        build_failed = True
    else:
        print("Application {} finished building!".format(app))
    threads.remove(app)

def build_all_applications():
    print("Starting to build applications!")
    threading.Thread(target=build_application, args=("order-service",)).start()
    threading.Thread(target=build_application, args=("orchestrator-service",)).start()
    threading.Thread(target=build_application, args=("product-service",)).start()
    threading.Thread(target=build_application, args=("payment-service",)).start()
    threading.Thread(target=build_application, args=("inventory-service",)).start()

def remove_remaining_containers():
    print("Removing all containers.")
    os.system("docker-compose down")
    containers = os.popen('docker ps -aq').read().split('\n')
    containers.remove('')
    if len(containers) > 0:
        print("There are still {} containers created".format(len(containers)))
        for container in containers:
            print("Stopping container {}".format(container))
            os.system("docker container stop {}".format(container))
        os.system("docker container prune -f")

def docker_compose_up():
    print("Running containers!")
    os.popen("docker-compose up --build -d").read()
    print("Pipeline finished!")

if __name__ == "__main__":
    print("Pipeline started!")
    build_all_applications()
    while len(threads) > 0:
        pass
    if build_failed:
        print("One or more builds failed. Docker Compose will not be started.")
    else:
        remove_remaining_containers()
        threading.Thread(target=docker_compose_up).start()