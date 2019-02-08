package net.heithoff

import net.heithoff.base.LegalName
import net.heithoff.base.PreferredName
import workday.com.bsvc.WorkerType

class Worker {
    WorkerType worker
    String descriptor
    String wid
    LegalName legalName = new LegalName()
    PreferredName preferredName = new PreferredName()

    Worker() {

    }

    Worker(WorkerType workerType) {
        worker = workerType
        descriptor = worker.getWorkerReference().getDescriptor()
        wid = workerType.workerReference.ID.properties.get("WID")
    }
}
