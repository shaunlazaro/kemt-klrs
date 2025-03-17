import Button from "../../components/button";
import Searchbar from "../../components/searchbar";
import { DataTable } from "../../components/datatable/data-table";
import { PatientTableColumnDef } from "../../components/datatable";
import { MockPatients } from "../../testData/patient";
import { MdAddCircleOutline } from "react-icons/md";
import { useNavigate } from "react-router-dom";
import { ADDEDIT_PATIENTS_PATH_NEW } from "../../routes";
import { useGetPatients } from "../../api/hooks";
import Loader from "../../components/loader/loader";

const Patients: React.FC = () => {
  const navigate = useNavigate();
  const { data: patientData, isLoading: isLoading } = useGetPatients();
  console.log(patientData)

  return (
    <>
      {isLoading && <Loader />}
      <div className="h-auto bg-white pb-20 px-8">
        <div className="text-3xl font-bold text-neutral-800 pb-4">
          Patients
        </div>
        <div className="w-full flex justify-between pb-4">
          <div>
            <span className="text-primary-darkblue text-sm font-semibold">Search</span><br />
            <Searchbar placeholder="Search by name" />
          </div>
          <Button variant="primary" className="h-auto mt-4 mb-1" onClick={() => { navigate(ADDEDIT_PATIENTS_PATH_NEW) }}>
            <MdAddCircleOutline className="h-auto w-6" />
            <span className="font-semibold">Add Patient</span>
          </Button>
        </div>
        <DataTable columns={PatientTableColumnDef} data={patientData ?? []} />
      </div>
    </>
  );
};

export default Patients;
