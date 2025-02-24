import Button from "../../components/button";
import Searchbar from "../../components/searchbar";
import { DataTable } from "../../components/datatable/data-table";
import { PatientTableColumnDef } from "../../components/datatable";
import { MockPatients } from "../../testData/patient";

const Patients: React.FC = () => {
    return (
      <div className="h-auto bg-white pb-20 px-8">
        <div className="text-3xl font-bold text-neutral-800 pb-4">
          Patients
        </div>
        <div className="w-full flex justify-between pb-4">
          <Searchbar placeholder="Search by name"/>
          <Button variant="primary">
            Add Patient
          </Button>
        </div>
        <DataTable columns={PatientTableColumnDef} data={MockPatients}/>
      </div>
    );
  };
  
export default Patients;
  