import Button from "../../components/button";
import Searchbar from "../../components/searchbar";
import { DataTable } from "../../components/datatable/data-table";
import { PatientTableColumnDef } from "../../components/datatable";
import { MockPatients } from "../../testData/patient";
import { MdAddCircleOutline } from "react-icons/md";

const Patients: React.FC = () => {
    return (
      <div className="h-auto bg-white pb-20 px-8">
        <div className="text-3xl font-bold text-neutral-800 pb-4">
          Patients
        </div>
        <div className="w-full flex justify-between pb-4">
          <div>
            <span className="text-primary-darkblue text-sm font-semibold">Search</span><br/>
            <Searchbar placeholder="Search by name"/>
          </div>
          <Button variant="primary" className="h-auto mt-4 mb-1">
            <MdAddCircleOutline className="h-auto w-6"/>
            <span className="font-semibold">Add Patient</span>
          </Button>
        </div>
        <DataTable columns={PatientTableColumnDef} data={MockPatients}/>
      </div>
    );
  };
  
export default Patients;
  